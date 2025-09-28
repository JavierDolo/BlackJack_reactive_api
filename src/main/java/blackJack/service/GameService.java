package blackJack.service;

import blackJack.domain.mongo.Game;
import blackJack.domain.mongo.GameRepository;
import blackJack.domain.mysql.Player;
import blackJack.dto.PlayRequest;
import blackJack.dto.PlayRequest.Action;
import blackJack.exception.BadRequestException;
import blackJack.exception.NotFoundException;
import blackJack.model.Card;
import blackJack.model.Deck;
import blackJack.model.GameStatus;
import blackJack.model.Hand;
import blackJack.model.Outcome;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class GameService {

    private final GameRepository games;
    private final PlayerService players;
    private final BlackjackEngine engine = new BlackjackEngine();

    public GameService(GameRepository games, PlayerService players) {
        this.games = games;
        this.players = players;
    }

    public Mono<Game> createNewGame(String playerName) {
        return players.findOrCreate(playerName)
                .flatMap(player -> {
                    Game g = new Game();
                    g.setPlayerId(player.getId());
                    g.setDeck(Deck.newShuffled());

                    // initial deal
                    List<Card> deck = g.getDeck();
                    g.getPlayerHand().add(deck.remove(0));
                    g.getDealerHand().add(deck.remove(0));
                    g.getPlayerHand().add(deck.remove(0));
                    g.getDealerHand().add(deck.remove(0));

                    Hand playerHand = new Hand(); playerHand.setCards(g.getPlayerHand());
                    Hand dealerHand = new Hand(); dealerHand.setCards(g.getDealerHand());

                    if (playerHand.isBlackjack()) {
                        g.setStatus(GameStatus.FINISHED);
                        g.setOutcome(Outcome.PLAYER_BLACKJACK);
                    } else {
                        g.setStatus(GameStatus.PLAYER_TURN);
                    }
                    g.setCreatedAt(Instant.now());
                    g.setUpdatedAt(Instant.now());
                    return games.save(g).flatMap(saved ->
                        // pay immediate blackjack 3:2 if player has it
                        (saved.getOutcome() == Outcome.PLAYER_BLACKJACK ?
                            players.recordWin(saved.getPlayerId(), new BigDecimal("1.5")) : Mono.just(new Player()))
                        .thenReturn(saved)
                    );
                });
    }

    public Mono<Game> get(String id) {
        return games.findById(id).switchIfEmpty(Mono.error(new NotFoundException("Game " + id + " not found")));
    }

    public Mono<Void> delete(String id) {
        return games.deleteById(id);
    }

    public Mono<Game> play(String id, PlayRequest req) {
        Action action = req.getAction();
        BigDecimal bet = req.getBet() == null ? BigDecimal.ONE : req.getBet();

        return get(id).flatMap(g -> {
            if (g.getStatus() == GameStatus.FINISHED) {
                return Mono.error(new BadRequestException("Game already finished"));
            }

            if (g.getBet().compareTo(BigDecimal.ZERO) == 0 && bet.compareTo(BigDecimal.ZERO) <= 0) {
                return Mono.error(new BadRequestException("Bet must be positive on first move"));
            }
            if (g.getBet().compareTo(BigDecimal.ZERO) == 0) {
                g.setBet(bet);
            }

            switch (action) {
                case HIT -> {
                    draw(g.getPlayerHand(), g.getDeck());
                    if (new Hand(){{
                        setCards(g.getPlayerHand());
                    }}.isBust()) {
                        g.setStatus(GameStatus.FINISHED);
                        g.setOutcome(Outcome.DEALER_WIN);
                        return endAndPersist(g, false);
                    } else {
                        g.setUpdatedAt(Instant.now());
                        return games.save(g);
                    }
                }
                case STAND -> {
                    dealerPlay(g);
                    g.setStatus(GameStatus.FINISHED);
                    g.setOutcome(engine.decideOutcome(g.getPlayerHand(), g.getDealerHand()));
                    return endAndPersist(g, g.getOutcome() == Outcome.PLAYER_WIN);
                }
                case DOUBLE -> {
                    if (g.getPlayerHand().size() != 2) {
                        return Mono.error(new BadRequestException("DOUBLE only allowed on first turn"));
                    }
                    g.setBet(g.getBet().multiply(new BigDecimal("2")));
                    draw(g.getPlayerHand(), g.getDeck());
                    if (new Hand(){{
                        setCards(g.getPlayerHand());
                    }}.isBust()) {
                        g.setStatus(GameStatus.FINISHED);
                        g.setOutcome(Outcome.DEALER_WIN);
                        return endAndPersist(g, false);
                    } else {
                        dealerPlay(g);
                        g.setStatus(GameStatus.FINISHED);
                        g.setOutcome(engine.decideOutcome(g.getPlayerHand(), g.getDealerHand()));
                        return endAndPersist(g, g.getOutcome() == Outcome.PLAYER_WIN);
                    }
                }
                default -> {
                    return Mono.error(new BadRequestException("Unsupported action"));
                }
            }
        });
    }

    private Mono<Game> endAndPersist(Game g, boolean playerWon) {
        g.setUpdatedAt(Instant.now());
        Mono<Game> saved = games.save(g);
        if (g.getBet() == null) g.setBet(BigDecimal.ONE);
        BigDecimal amount = g.getBet();
        return saved.flatMap(s -> (playerWon
                ? players.recordWin(s.getPlayerId(), amount)
                : players.recordLoss(s.getPlayerId(), amount)
        ).thenReturn(s));
    }

    private void dealerPlay(Game g) {
        while (engine.dealerShouldHit(g.getDealerHand())) {
            draw(g.getDealerHand(), g.getDeck());
        }
    }

    private void draw(List<Card> hand, List<Card> deck) {
        if (deck.isEmpty()) throw new IllegalStateException("Deck exhausted");
        hand.add(deck.remove(0));
    }
}

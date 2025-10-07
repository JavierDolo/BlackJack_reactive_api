package blackjack.service;

import blackjack.domain.mongo.Game;
import blackjack.domain.mongo.GameRepository;
import blackjack.dto.PlayRequest;
import blackjack.dto.PlayRequest.Action;
import blackjack.exception.BadRequestException;
import blackjack.exception.NotFoundException;
import blackjack.model.Card;
import blackjack.model.Deck;
import blackjack.model.GameStatus;
import blackjack.model.Hand;
import blackjack.model.Outcome;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
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

                    Hand playerHand = new Hand();
                    playerHand.setCards(g.getPlayerHand());

                    if (playerHand.isBlackjack()) {
                        g.setStatus(GameStatus.FINISHED);
                        g.setOutcome(Outcome.PLAYER_BLACKJACK);
                    } else {
                        g.setStatus(GameStatus.PLAYER_TURN);
                    }

                    return games.save(g).flatMap(saved -> {
                        if (saved.getOutcome() == Outcome.PLAYER_BLACKJACK) {
                            BigDecimal payout = saved.getBet().multiply(BigDecimal.valueOf(1.5));
                            return players.recordWin(saved.getPlayerId(), payout).thenReturn(saved);
                        }
                        return Mono.just(saved);
                    });
                });
    }

    public Mono<Game> get(String id) {
        return games.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Game " + id + " not found")));
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
                    Hand playerHand = new Hand();
                    playerHand.setCards(g.getPlayerHand());
                    if (playerHand.isBust()) {
                        g.setStatus(GameStatus.FINISHED);
                        g.setOutcome(Outcome.DEALER_WIN);
                        return endAndPersist(g, false);
                    } else {
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
                    g.setBet(g.getBet().multiply(BigDecimal.valueOf(2)));
                    draw(g.getPlayerHand(), g.getDeck());

                    Hand handAfterDouble = new Hand();
                    handAfterDouble.setCards(g.getPlayerHand());
                    if (handAfterDouble.isBust()) {
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
        Mono<Game> saved = games.save(g);
        BigDecimal amount = g.getBet().compareTo(BigDecimal.ZERO) > 0 ? g.getBet() : BigDecimal.ONE;
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
        if (deck.isEmpty()) {
            throw new BadRequestException("Deck exhausted");
        }
        hand.add(deck.remove(0));
    }
}

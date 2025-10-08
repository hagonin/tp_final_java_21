package fr.digi.d202508.tp_final_java21.controller;

import fr.digi.d202508.tp_final_java21.model.Animal;
import fr.digi.d202508.tp_final_java21.model.RaceStatistics;
import fr.digi.d202508.tp_final_java21.service.Race;
import fr.digi.d202508.tp_final_java21.service.RaceTrack;
import fr.digi.d202508.tp_final_java21.service.Referee;
import fr.digi.d202508.tp_final_java21.service.Tournament;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the race GUI with speed indicators
 */
public class RaceController implements Initializable {

    @FXML private VBox raceTrackContainer;
    @FXML private Button startRaceButton;
    @FXML private Button startTournamentButton;
    @FXML private TextArea resultsArea;
    @FXML private Label raceStatusLabel;
    @FXML private CheckBox useRefereeCheckBox;
    @FXML private Spinner<Integer> roundsSpinner;
    
    // Speed indicator components
    @FXML private ProgressBar tortueSpeedBar;
    @FXML private ProgressBar lapinSpeedBar;
    @FXML private ProgressBar chevalSpeedBar;
    @FXML private Label tortueSpeedLabel;
    @FXML private Label lapinSpeedLabel;
    @FXML private Label chevalSpeedLabel;
    @FXML private Label tortuePositionLabel;
    @FXML private Label lapinPositionLabel;
    @FXML private Label chevalPositionLabel;

    // Visual state indicators (circles)
    @FXML private Circle tortueStateCircle;
    @FXML private Circle lapinStateCircle;
    @FXML private Circle chevalStateCircle;

    // State labels (boost/fatigue indicators)
    @FXML private Label tortueStateLabel;
    @FXML private Label lapinStateLabel;
    @FXML private Label chevalStateLabel;

    // Race track visual components
    private Rectangle tortueRect;
    private Rectangle lapinRect;
    private Rectangle chevalRect;
    private Rectangle finishLine;
    
    // Race management
    private Race currentRace;
    private Tournament currentTournament;
    private AnimationTimer raceAnimationTimer;
    private Referee referee;
    private Thread refereeThread;
    
    // Constants for display
    private static final double TRACK_WIDTH = 700;
    private static final double TRACK_HEIGHT = 50;
    private static final double ANIMAL_SIZE = 18;
    private static final double MAX_SPEED = 60.0; // Maximum expected speed for normalization

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUI();
        setupRaceTrack();
        resetSpeedIndicators();
    }

    private void setupUI() {
        roundsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3));
        raceStatusLabel.setText("Prêt à commencer");
        resultsArea.setEditable(false);
    }

    private void setupRaceTrack() {
        raceTrackContainer.getChildren().clear();
        
        // Create visual tracks for each animal
        createAnimalTrack("🐢 Tortue", Color.GREEN);
        createAnimalTrack("🐰 Lapin", Color.BROWN);
        createAnimalTrack("🐴 Cheval", Color.DARKBLUE);
        
        // Store references to animal rectangles
        tortueRect = (Rectangle) raceTrackContainer.getChildren().get(0).lookup(".animal-rect");
        lapinRect = (Rectangle) raceTrackContainer.getChildren().get(1).lookup(".animal-rect");
        chevalRect = (Rectangle) raceTrackContainer.getChildren().get(2).lookup(".animal-rect");
    }

    private void createAnimalTrack(String animalName, Color color) {
        VBox trackContainer = new VBox(5);
        
        // Animal name label
        Label nameLabel = new Label(animalName);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        // Track background
        Rectangle trackBg = new Rectangle(TRACK_WIDTH, TRACK_HEIGHT);
        trackBg.setFill(Color.LIGHTGRAY);
        trackBg.setStroke(Color.BLACK);
        
        // Finish line
        Rectangle finish = new Rectangle(TRACK_WIDTH - 5, 0, 5, TRACK_HEIGHT);
        finish.setFill(Color.RED);
        
        // Animal representation
        Rectangle animalRect = new Rectangle(ANIMAL_SIZE, ANIMAL_SIZE);
        animalRect.setFill(color);
        animalRect.setX(0);
        animalRect.setY((TRACK_HEIGHT - ANIMAL_SIZE) / 2);
        animalRect.getStyleClass().add("animal-rect");
        
        // Container for track elements
        VBox trackElements = new VBox();
        trackElements.getChildren().addAll(trackBg, finish, animalRect);
        
        trackContainer.getChildren().addAll(nameLabel, trackElements);
        raceTrackContainer.getChildren().add(trackContainer);
    }

    @FXML
    private void startSingleRace() {
        if (currentRace != null && raceAnimationTimer != null) {
            return; // Race already running
        }
        
        startRaceButton.setDisable(true);
        startTournamentButton.setDisable(true);
        raceStatusLabel.setText("Course en cours...");
        resultsArea.clear();
        resetAnimalPositions();
        resetSpeedIndicators();
        
        // Create and initialize race
        currentRace = new Race();
        currentRace.initializeRace();
        
        // Start referee if enabled
        if (useRefereeCheckBox.isSelected()) {
            referee = new Referee(currentRace.getRaceTrack(), 2000);
            refereeThread = new Thread(referee, "Thread-Referee");
            refereeThread.start();
        }
        
        // Start race in background thread
        Thread raceThread = new Thread(() -> {
            currentRace.startRace();
            startRaceAnimation();
            currentRace.monitorRace();
            
            Platform.runLater(() -> {
                stopRaceAnimation();
                displayRaceResults();
                enableButtons();
            });
        });
        
        raceThread.setDaemon(true);
        raceThread.start();
    }

    @FXML
    private void startTournament() {
        if (currentTournament != null) {
            return; // Tournament already running
        }
        
        startRaceButton.setDisable(true);
        startTournamentButton.setDisable(true);
        raceStatusLabel.setText("Tournoi en cours...");
        resultsArea.clear();
        
        int rounds = roundsSpinner.getValue();
        boolean useReferee = useRefereeCheckBox.isSelected();
        
        // Create and start tournament in background thread
        Thread tournamentThread = new Thread(() -> {
            currentTournament = new Tournament(rounds, useReferee);
            
            // Run tournament with GUI updates
            runTournamentWithGUI(rounds, useReferee);
            
            Platform.runLater(() -> {
                raceStatusLabel.setText("Tournoi terminé");
                enableButtons();
                currentTournament = null;
            });
        });
        
        tournamentThread.setDaemon(true);
        tournamentThread.start();
    }

    private void runTournamentWithGUI(int totalRounds, boolean useReferee) {
        RaceStatistics statistics = new RaceStatistics();
        
        for (int round = 1; round <= totalRounds; round++) {
            final int currentRound = round;
            
            Platform.runLater(() -> {
                raceStatusLabel.setText(String.format("Tournoi - Manche %d/%d", currentRound, totalRounds));
                resetAnimalPositions();
                resetSpeedIndicators();
            });
            
            // Create and run race for this round
            Race race = new Race();
            race.initializeRace();
            
            // Start referee if enabled
            if (useReferee) {
                referee = new Referee(race.getRaceTrack(), 2000);
                refereeThread = new Thread(referee, "Thread-Referee");
                refereeThread.start();
            }
            
            race.startRace();
            
            Platform.runLater(() -> startRaceAnimationForRace(race));
            
            race.monitorRace();
            
            Platform.runLater(() -> stopRaceAnimation());
            
            // Stop referee
            if (referee != null) {
                referee.stop();
                if (refereeThread != null && refereeThread.isAlive()) {
                    refereeThread.interrupt();
                }
            }
            
            // Record results
            List<Animal> scoreboard = race.getRaceTrack().getFinalScoreboard();
            statistics.recordRound(scoreboard);
            
            // Update results display
            Platform.runLater(() -> {
                updateTournamentResults(currentRound, totalRounds, statistics, scoreboard);
            });
            
            // Wait between rounds (except last one)
            if (round < totalRounds) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        // Display final results
        Platform.runLater(() -> displayFinalTournamentResults(statistics));
    }

    private void startRaceAnimation() {
        if (currentRace != null) {
            startRaceAnimationForRace(currentRace);
        }
    }

    private void startRaceAnimationForRace(Race race) {
        if (raceAnimationTimer != null) {
            raceAnimationTimer.stop();
        }
        
        raceAnimationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateRaceDisplay(race.getRaceTrack());
            }
        };
        raceAnimationTimer.start();
    }

    private void stopRaceAnimation() {
        if (raceAnimationTimer != null) {
            raceAnimationTimer.stop();
            raceAnimationTimer = null;
        }
    }

    private void updateRaceDisplay(RaceTrack raceTrack) {
        List<Animal> participants = raceTrack.getParticipants();
        
        for (Animal animal : participants) {
            double progress = animal.getPosition() / RaceTrack.FINISH_LINE;
            double xPosition = Math.min(progress * (TRACK_WIDTH - ANIMAL_SIZE), TRACK_WIDTH - ANIMAL_SIZE);
            
            // Update position on track
            if (animal.getName().equals("Tortue")) {
                tortueRect.setX(xPosition);
                updateSpeedIndicator(tortueSpeedBar, tortueSpeedLabel, tortuePositionLabel, animal);
            } else if (animal.getName().equals("Lapin")) {
                lapinRect.setX(xPosition);
                updateSpeedIndicator(lapinSpeedBar, lapinSpeedLabel, lapinPositionLabel, animal);
            } else if (animal.getName().equals("Cheval")) {
                chevalRect.setX(xPosition);
                updateSpeedIndicator(chevalSpeedBar, chevalSpeedLabel, chevalPositionLabel, animal);
            }
        }
    }

    private void updateSpeedIndicator(ProgressBar speedBar, Label speedLabel, Label positionLabel, Animal animal) {
        // Get references to state indicators based on animal name
        Circle stateCircle = null;
        Label stateLabel = null;

        if (animal.getName().equals("Tortue")) {
            stateCircle = tortueStateCircle;
            stateLabel = tortueStateLabel;
        } else if (animal.getName().equals("Lapin")) {
            stateCircle = lapinStateCircle;
            stateLabel = lapinStateLabel;
        } else if (animal.getName().equals("Cheval")) {
            stateCircle = chevalStateCircle;
            stateLabel = chevalStateLabel;
        }

        double normalizedSpeed = animal.getCurrentSpeed() / MAX_SPEED;
        speedBar.setProgress(Math.min(normalizedSpeed, 1.0));

        // Determine speed state based on base speed comparison
        double speedRatio = animal.getCurrentSpeed() / animal.getSpeed();
        String speedState;
        Color circleColor;
        String speedTextColor;

        if (speedRatio > 1.2) {
            // BOOST state (> 120% of base speed)
            speedState = "🚀 BOOST!";
            circleColor = Color.rgb(76, 175, 80); // Green
            speedTextColor = "#4CAF50"; // Green text
            speedBar.setStyle("-fx-accent: #4CAF50;"); // Green progress bar
        } else if (speedRatio < 0.8) {
            // FATIGUE state (< 80% of base speed)
            speedState = "😴 Fatigue";
            circleColor = Color.rgb(244, 67, 54); // Red
            speedTextColor = "#F44336"; // Red text
            speedBar.setStyle("-fx-accent: #F44336;"); // Red progress bar
        } else {
            // NORMAL state
            speedState = "🤖";
            circleColor = Color.rgb(33, 150, 243); // Blue
            speedTextColor = "#2196F3"; // Blue text
            speedBar.setStyle("-fx-accent: #2196F3;"); // Blue progress bar
        }

        // Update state circle and label
        if (stateCircle != null) {
            stateCircle.setFill(circleColor);
        }
        if (stateLabel != null) {
            stateLabel.setText(speedState);
            stateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        }

        // Update speed label with color coding
        speedLabel.setText(String.format("%.1f km/h", animal.getCurrentSpeed()));
        speedLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-weight: bold; -fx-font-size: 14px;", speedTextColor));

        positionLabel.setText(String.format("%.1f m", animal.getPosition()));
    }

    private void resetSpeedIndicators() {
        tortueSpeedBar.setProgress(0);
        lapinSpeedBar.setProgress(0);
        chevalSpeedBar.setProgress(0);

        tortueSpeedLabel.setText("0.0 km/h");
        lapinSpeedLabel.setText("0.0 km/h");
        chevalSpeedLabel.setText("0.0 km/h");

        tortuePositionLabel.setText("0.0 m");
        lapinPositionLabel.setText("0.0 m");
        chevalPositionLabel.setText("0.0 m");

        // Reset state indicators
        if (tortueStateCircle != null) {
            tortueStateCircle.setFill(Color.LIGHTGRAY);
            tortueStateLabel.setText("—");
        }
        if (lapinStateCircle != null) {
            lapinStateCircle.setFill(Color.LIGHTGRAY);
            lapinStateLabel.setText("—");
        }
        if (chevalStateCircle != null) {
            chevalStateCircle.setFill(Color.LIGHTGRAY);
            chevalStateLabel.setText("—");
        }
    }

    private void resetAnimalPositions() {
        if (tortueRect != null) tortueRect.setX(0);
        if (lapinRect != null) lapinRect.setX(0);
        if (chevalRect != null) chevalRect.setX(0);
    }

    private void displayRaceResults() {
        if (currentRace != null) {
            Animal winner = currentRace.getRaceTrack().getWinner();
            List<Animal> scoreboard = currentRace.getRaceTrack().getFinalScoreboard();
            
            StringBuilder results = new StringBuilder();
            results.append("=== RÉSULTATS DE LA COURSE ===\n\n");
            
            if (winner != null) {
                results.append("🏆 GAGNANT: ").append(winner.getName()).append("\n\n");
            }
            
            results.append("Classement final:\n");
            for (int i = 0; i < scoreboard.size(); i++) {
                Animal animal = scoreboard.get(i);
                String medal = switch (i) {
                    case 0 -> "🥇";
                    case 1 -> "🥈";
                    case 2 -> "🥉";
                    default -> "  ";
                };
                results.append(String.format("%s %d. %s - %.2f km/h (%.1f m)\n", 
                    medal, i + 1, animal.getName(), animal.getSpeed(), animal.getPosition()));
            }
            
            resultsArea.setText(results.toString());
            raceStatusLabel.setText("Course terminée");
        }
    }

    private void updateTournamentResults(int currentRound, int totalRounds, RaceStatistics statistics, List<Animal> scoreboard) {
        StringBuilder results = new StringBuilder();
        results.append(String.format("=== TOURNOI - MANCHE %d/%d ===\n\n", currentRound, totalRounds));
        
        // Current round results
        if (!scoreboard.isEmpty()) {
            results.append("Résultat de la manche:\n");
            Animal winner = scoreboard.get(0);
            results.append(" Gagnant: ").append(winner.getName()).append("\n\n");
        }
        
        // Overall standings
        results.append("Classement général:\n");
        List<RaceStatistics.AnimalStats> standings = statistics.getAllStatsSortedByWins();
        for (int i = 0; i < standings.size(); i++) {
            RaceStatistics.AnimalStats stats = standings.get(i);
            String medal = switch (i) {
                case 0 -> "🥇";
                case 1 -> "🥈";
                case 2 -> "🥉";
                default -> "  ";
            };
            results.append(String.format("%s %s - Victoires: %d (%.1f%%)\n", 
                medal, stats.getName(), stats.getWins(), stats.getWinRate()));
        }
        
        resultsArea.setText(results.toString());
    }

    private void displayFinalTournamentResults(RaceStatistics statistics) {
        StringBuilder results = new StringBuilder();
        results.append("=== RÉSULTATS FINAUX DU TOURNOI ===\n\n");
        
        List<RaceStatistics.AnimalStats> finalStandings = statistics.getAllStatsSortedByWins();
        if (!finalStandings.isEmpty()) {
            RaceStatistics.AnimalStats champion = finalStandings.get(0);
            results.append(" CHAMPION DU TOURNOI: ").append(champion.getName()).append(" 🏆\n");
            results.append(String.format("Victoires: %d (%.1f%% de réussite)\n\n", 
                champion.getWins(), champion.getWinRate()));
        }
        
        results.append("Classement final:\n");
        for (int i = 0; i < finalStandings.size(); i++) {
            RaceStatistics.AnimalStats stats = finalStandings.get(i);
            String medal = switch (i) {
                case 0 -> "🥇";
                case 1 -> "🥈";
                case 2 -> "🥉";
                default -> "  ";
            };
            results.append(String.format("%s %s - Victoires: %d, Participations: %d (%.1f%%)\n", 
                medal, stats.getName(), stats.getWins(), stats.getSpeeds().size(), stats.getWinRate()));
        }
        
        resultsArea.setText(results.toString());
    }

    private void enableButtons() {
        startRaceButton.setDisable(false);
        startTournamentButton.setDisable(false);
        currentRace = null;
    }
}
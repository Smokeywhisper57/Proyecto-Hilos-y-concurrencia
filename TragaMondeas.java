package com.mycompany.slotmachine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Aplicación principal del juego de tragamonedas
 * Implementa el patrón MVC con manejo de concurrencia
 */
public class SlotMachineApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Configuración inicial de símbolos con sus valores
            Symbol[] gameSymbols = {
                new Symbol("🍒", 10),  // Cereza - mayor valor
                new Symbol("🍋", 5),   // Limón - valor medio
                new Symbol("🍊", 3),   // Naranja - valor bajo
                new Symbol("⭐", 1)     // Estrella - valor especial
            };

            // Creación de los tres rodillos del juego
            Reel[] gameReels = { 
                new Reel(gameSymbols), 
                new Reel(gameSymbols), 
                new Reel(gameSymbols) 
            };
            
            // Inicialización del modelo, vista y controlador
            SlotMachineModel gameModel = new SlotMachineModel(gameReels);
            SlotMachineView gameView = new SlotMachineView();
            SlotMachineController gameController = new SlotMachineController(gameModel, gameView);

            // Configuración del pool de hilos para manejo concurrente
            ExecutorService threadPool = Executors.newFixedThreadPool(2);
            threadPool.execute(new GameEngineThread(gameController));
        });
    }
}

/**
 * Representa un símbolo que aparece en los rodillos del juego
 */
class Symbol {
    private final String displayCharacter;
    private final int payoutMultiplier;

    public Symbol(String displayCharacter, int payoutMultiplier) {
        this.displayCharacter = displayCharacter;
        this.payoutMultiplier = payoutMultiplier;
    }

    public String getDisplayCharacter() {
        return displayCharacter;
    }

    public int getPayoutMultiplier() {
        return payoutMultiplier;
    }
}

/**
 * Representa un rodillo individual que puede girar y mostrar símbolos
 */
class Reel {
    private final Symbol[] availableSymbols;

    public Reel(Symbol[] availableSymbols) {
        this.availableSymbols = availableSymbols;
    }

    /**
     * Simula el giro del rodillo y devuelve un símbolo aleatorio
     */
    public Symbol spin() {
        Random randomGenerator = new Random();
        return availableSymbols[randomGenerator.nextInt(availableSymbols.length)];
    }
}

/**
 * Modelo principal del juego que contiene la lógica y estado
 */
class SlotMachineModel {
    private final Reel[] gameReels;
    private int currentBet;
    private int playerCredits = 100;

    public SlotMachineModel(Reel[] gameReels) {
        this.gameReels = gameReels;
    }

    public void setCurrentBet(int newBet) {
        this.currentBet = newBet;
    }

    public int getPlayerCredits() {
        return playerCredits;
    }

    /**
     * Verifica si el jugador puede realizar un giro
     */
    public boolean isSpinAllowed() {
        return playerCredits >= currentBet && currentBet > 0;
    }

    /**
     * Ejecuta un giro y calcula el resultado
     */
    public SpinOutcome executeSpin() {
        if (!isSpinAllowed()) {
            return new SpinOutcome(null, 0, playerCredits);
        }
        
        playerCredits -= currentBet;
        Symbol[] spinResults = new Symbol[gameReels.length];
        
        for (int i = 0; i < gameReels.length; i++) {
            spinResults[i] = gameReels[i].spin();
        }
        
        int winAmount = calculateWinAmount(spinResults);
        playerCredits += winAmount;
        
        return new SpinOutcome(spinResults, winAmount, playerCredits);
    }

    /**
     * Calcula el monto ganado basado en los símbolos obtenidos
     */
    private int calculateWinAmount(Symbol[] spinResults) {
        return allSymbolsIdentical(spinResults) ? 
               spinResults[0].getPayoutMultiplier() * currentBet : 0;
    }

    private boolean allSymbolsIdentical(Symbol[] symbols) {
        String firstSymbol = symbols[0].getDisplayCharacter();
        for (Symbol symbol : symbols) {
            if (!symbol.getDisplayCharacter().equals(firstSymbol)) return false;
        }
        return true;
    }
}

/**
 * Contiene los resultados de un giro del tragamonedas
 */
class SpinOutcome {
    private final Symbol[] resultingSymbols;
    private final int creditsWon;
    private final int totalCredits;

    public SpinOutcome(Symbol[] resultingSymbols, int creditsWon, int totalCredits) {
        this.resultingSymbols = resultingSymbols;
        this.creditsWon = creditsWon;
        this.totalCredits = totalCredits;
    }

    public Symbol[] getResultingSymbols() {
        return resultingSymbols;
    }

    public int getCreditsWon() {
        return creditsWon;
    }

    public int getTotalCredits() {
        return totalCredits;
    }
}

/**
 * Vista del juego que maneja la interfaz gráfica
 */
class SlotMachineView {
    private final JFrame mainWindow;
    private final JLabel[] reelDisplayLabels;
    private final JLabel creditsDisplay;
    private final JLabel resultMessage;
    private final JTextField betInputField;
    private final JButton spinButton;
    private final JButton increaseBetButton;
    private final JButton decreaseBetButton;

    public SlotMachineView() {
        mainWindow = new JFrame("Tragamonedas MVC");
        reelDisplayLabels = new JLabel[3];
        creditsDisplay = new JLabel("Créditos: ");
        resultMessage = new JLabel("");
        betInputField = new JTextField("1", 5);
        spinButton = new JButton("GIRAR");
        increaseBetButton = new JButton("+");
        decreaseBetButton = new JButton("-");
        
        initializeInterface();
    }

    private void initializeInterface() {
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setSize(600, 400);
        mainWindow.setLayout(new BorderLayout());
        mainWindow.setLocationRelativeTo(null);

        JPanel reelsPanel = createReelsDisplayPanel();
        JPanel controlsPanel = createControlButtonsPanel();

        mainWindow.add(reelsPanel, BorderLayout.CENTER);
        mainWindow.add(controlsPanel, BorderLayout.SOUTH);
        mainWindow.setVisible(true);
    }

    private JPanel createReelsDisplayPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        for (int i = 0; i < 3; i++) {
            reelDisplayLabels[i] = new JLabel("", SwingConstants.CENTER);
            reelDisplayLabels[i].setFont(new Font("Segoe UI Emoji", Font.BOLD, 60));
            reelDisplayLabels[i].setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
            panel.add(reelDisplayLabels[i]);
        }
        return panel;
    }

    private JPanel createControlButtonsPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
        
        JPanel creditsPanel = new JPanel();
        creditsPanel.add(creditsDisplay);
        
        JPanel betControlsPanel = new JPanel();
        betControlsPanel.add(new JLabel("Apuesta:"));
        betControlsPanel.add(betInputField);
        betControlsPanel.add(decreaseBetButton);
        betControlsPanel.add(increaseBetButton);
        
        JPanel resultPanel = new JPanel();
        resultPanel.add(resultMessage);

        panel.add(creditsPanel);
        panel.add(betControlsPanel);
        panel.add(spinButton);
        panel.add(resultPanel);
        
        return panel;
    }

    // Configuración de listeners
    public void setSpinButtonAction(ActionListener listener) {
        spinButton.addActionListener(listener);
    }

    public void setBetIncreaseAction(ActionListener listener) {
        increaseBetButton.addActionListener(listener);
    }

    public void setBetDecreaseAction(ActionListener listener) {
        decreaseBetButton.addActionListener(listener);
    }

    // Métodos para manejar la apuesta
    public int getCurrentBet() {
        try {
            return Integer.parseInt(betInputField.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void updateBetDisplay(int newBet) {
        betInputField.setText(String.valueOf(newBet));
    }

    /**
     * Actualiza la vista con los resultados de un giro
     */
    public void showSpinResults(SpinOutcome outcome) {
        if (outcome.getResultingSymbols() != null) {
            for (int i = 0; i < outcome.getResultingSymbols().length; i++) {
                reelDisplayLabels[i].setText(outcome.getResultingSymbols()[i].getDisplayCharacter());
            }
        }
        creditsDisplay.setText("Créditos: " + outcome.getTotalCredits());
        resultMessage.setText(outcome.getCreditsWon() > 0 ? 
            "¡Ganaste " + outcome.getCreditsWon() + " créditos!" : "Intenta de nuevo.");
    }

    public void toggleSpinButton(boolean isEnabled) {
        spinButton.setEnabled(isEnabled);
    }
}

/**
 * Controlador que maneja la interacción entre el modelo y la vista
 */
class SlotMachineController {
    private final SlotMachineModel gameModel;
    private final SlotMachineView gameView;

    public SlotMachineController(SlotMachineModel gameModel, SlotMachineView gameView) {
        this.gameModel = gameModel;
        this.gameView = gameView;
        configureEventHandlers();
        refreshView();
    }

    private void configureEventHandlers() {
        gameView.setSpinButtonAction(e -> handleSpin());
        gameView.setBetIncreaseAction(e -> modifyBet(1));
        gameView.setBetDecreaseAction(e -> modifyBet(-1));
    }

    private void handleSpin() {
        gameView.toggleSpinButton(false);
        
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                return null;
            }
            
            @Override
            protected void done() {
                updateAfterSpin();
            }
        }.execute();
    }

    private void updateAfterSpin() {
        SpinOutcome spinResult = gameModel.executeSpin();
        gameView.showSpinResults(spinResult);
        gameView.toggleSpinButton(true);
    }

    private void modifyBet(int adjustment) {
        int proposedBet = gameView.getCurrentBet() + adjustment;
        if (proposedBet > 0 && proposedBet <= gameModel.getPlayerCredits()) {
            gameModel.setCurrentBet(proposedBet);
            gameView.updateBetDisplay(proposedBet);
        }
    }

    private void refreshView() {
        gameView.showSpinResults(new SpinOutcome(null, 0, gameModel.getPlayerCredits()));
        gameModel.setCurrentBet(gameView.getCurrentBet());
    }
}

/**
 * Hilo de ejecución para operaciones concurrentes del juego
 */
class GameEngineThread implements Runnable {
    private final SlotMachineController mainController;

    public GameEngineThread(SlotMachineController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void run() {
        // Lógica de fondo para operaciones concurrentes
    }
}

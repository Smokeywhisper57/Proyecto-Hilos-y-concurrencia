package com.mycompany.tragmone;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clase principal que inicia la aplicación del tragamonedas
 * Patrón MVC + Concurrencia
 */
public class TragMone {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Symbol[] symbols = {
                new Symbol("🍒", 10),
                new Symbol("🍋", 5),
                new Symbol("🍊", 3),
                new Symbol("⭐", 1)
            };

            Reel[] reels = { new Reel(symbols), new Reel(symbols), new Reel(symbols) };
            SlotMachine model = new SlotMachine(reels);
            SlotMachineViewGUI view = new SlotMachineViewGUI();
            SlotMachineController controller = new SlotMachineController(model, view);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.execute(new GameThread(controller));
        });
    }
}

/**
 * Representa un símbolo en los rodillos del tragamonedas
 */
class Symbol {
    private final String name;
    private final int value;

    public Symbol(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }
}

/**
 * Representa un rodillo con símbolos que puede girar
 */
class Reel {
    private final Symbol[] symbols;

    public Reel(Symbol[] symbols) {
        this.symbols = symbols;
    }

    /**
     * Genera un resultado aleatorio del rodillo
     */
    public Symbol spin() {
        return symbols[new Random().nextInt(symbols.length)];
    }
}

/**
 * Modelo principal del tragamonedas (MVC)
 * Contiene la lógica del juego y estado
 */
class SlotMachine {
    private final Reel[] reels;
    private int bet;
    private int credits = 100;

    public SlotMachine(Reel[] reels) {
        this.reels = reels;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    public int getCredits() {
        return credits;
    }

    /**
     * Verifica si se puede realizar un giro
     */
    public boolean canSpin() {
        return credits >= bet && bet > 0;
    }

    /**
     * Realiza un giro y calcula los resultados
     */
    public SpinResult spin() {
        if (!canSpin()) {
            return new SpinResult(null, 0, credits);
        }
        
        credits -= bet;
        Symbol[] results = new Symbol[reels.length];
        
        for (int i = 0; i < reels.length; i++) {
            results[i] = reels[i].spin();
        }
        
        int winnings = calculateWinnings(results);
        credits += winnings;
        
        return new SpinResult(results, winnings, credits);
    }

    /**
     * Calcula las ganancias basado en los símbolos
     */
    private int calculateWinnings(Symbol[] symbols) {
        return allSymbolsMatch(symbols) ? symbols[0].getValue() * bet : 0;
    }

    private boolean allSymbolsMatch(Symbol[] symbols) {
        String first = symbols[0].getName();
        for (Symbol symbol : symbols) {
            if (!symbol.getName().equals(first)) return false;
        }
        return true;
    }
}

/**
 * Contiene el resultado de un giro
 */
class SpinResult {
    private final Symbol[] symbols;
    private final int winnings;
    private final int credits;

    public SpinResult(Symbol[] symbols, int winnings, int credits) {
        this.symbols = symbols;
        this.winnings = winnings;
        this.credits = credits;
    }

    public Symbol[] getSymbols() {
        return symbols;
    }

    public int getWinnings() {
        return winnings;
    }

    public int getCredits() {
        return credits;
    }
}

/**
 * Vista del tragamonedas (MVC)
 * Maneja la interfaz gráfica
 */
class SlotMachineViewGUI {
    private final JFrame frame;
    private final JLabel[] reelLabels;
    private final JLabel creditsLabel;
    private final JLabel resultLabel;
    private final JTextField betField;
    private final JButton spinButton;
    private final JButton increaseBetButton;
    private final JButton decreaseBetButton;

    public SlotMachineViewGUI() {
        frame = new JFrame("Tragamonedas MVC");
        reelLabels = new JLabel[3];
        creditsLabel = new JLabel("Créditos: ");
        resultLabel = new JLabel("");
        betField = new JTextField("1", 5);
        spinButton = new JButton("GIRAR");
        increaseBetButton = new JButton("+");
        decreaseBetButton = new JButton("-");
        
        createUI();
    }

    private void createUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        JPanel reelsPanel = createReelsPanel();
        JPanel controlPanel = createControlPanel();

        frame.add(reelsPanel, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private JPanel createReelsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        for (int i = 0; i < 3; i++) {
            reelLabels[i] = new JLabel("", SwingConstants.CENTER);
            reelLabels[i].setFont(new Font("Segoe UI Emoji", Font.BOLD, 60));
            reelLabels[i].setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
            panel.add(reelLabels[i]);
        }
        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
        
        JPanel creditsPanel = new JPanel();
        creditsPanel.add(creditsLabel);
        
        JPanel betPanel = new JPanel();
        betPanel.add(new JLabel("Apuesta:"));
        betPanel.add(betField);
        betPanel.add(decreaseBetButton);
        betPanel.add(increaseBetButton);
        
        JPanel resultPanel = new JPanel();
        resultPanel.add(resultLabel);

        panel.add(creditsPanel);
        panel.add(betPanel);
        panel.add(spinButton);
        panel.add(resultPanel);
        
        return panel;
    }

    // Listeners
    public void setSpinButtonListener(ActionListener listener) {
        spinButton.addActionListener(listener);
    }

    public void setIncreaseBetListener(ActionListener listener) {
        increaseBetButton.addActionListener(listener);
    }

    public void setDecreaseBetListener(ActionListener listener) {
        decreaseBetButton.addActionListener(listener);
    }

    // Getters/Setters
    public int getBet() {
        try {
            return Integer.parseInt(betField.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void setBet(int bet) {
        betField.setText(String.valueOf(bet));
    }

    public void displaySpinResult(SpinResult result) {
        if (result.getSymbols() != null) {
            for (int i = 0; i < result.getSymbols().length; i++) {
                reelLabels[i].setText(result.getSymbols()[i].getName());
            }
        }
        creditsLabel.setText("Créditos: " + result.getCredits());
        resultLabel.setText(result.getWinnings() > 0 ? 
            "¡Ganaste " + result.getWinnings() + " créditos!" : "Intenta de nuevo.");
    }

    public void setSpinEnabled(boolean enabled) {
        spinButton.setEnabled(enabled);
    }
}

/**
 * Controlador del tragamonedas (MVC)
 * Maneja la interacción entre Modelo y Vista
 */
class SlotMachineController {
    private final SlotMachine model;
    private final SlotMachineViewGUI view;

    public SlotMachineController(SlotMachine model, SlotMachineViewGUI view) {
        this.model = model;
        this.view = view;
        setupListeners();
        updateView();
    }

    private void setupListeners() {
        view.setSpinButtonListener(e -> spin());
        view.setIncreaseBetListener(e -> adjustBet(1));
        view.setDecreaseBetListener(e -> adjustBet(-1));
    }

    private void spin() {
        view.setSpinEnabled(false);
        
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                return null;
            }
            
            @Override
            protected void done() {
                processSpinResult();
            }
        }.execute();
    }

    private void processSpinResult() {
        SpinResult result = model.spin();
        view.displaySpinResult(result);
        view.setSpinEnabled(true);
    }

    private void adjustBet(int delta) {
        int newBet = view.getBet() + delta;
        if (newBet > 0 && newBet <= model.getCredits()) {
            model.setBet(newBet);
            view.setBet(newBet);
        }
    }

    private void updateView() {
        view.displaySpinResult(new SpinResult(null, 0, model.getCredits()));
        model.setBet(view.getBet());
    }
}

/**
 * Hilo de ejecución para la concurrencia
 */
class GameThread implements Runnable {
    private final SlotMachineController controller;

    public GameThread(SlotMachineController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        // Lógica de fondo si fuera necesaria
    }
}

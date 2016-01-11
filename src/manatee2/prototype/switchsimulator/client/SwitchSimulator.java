package manatee2.prototype.switchsimulator.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import manatee2.prototype.switchsimulator.server.SwitchService;
import manatee2.prototype.switchsimulator.shared.SwitchConfiguration;


public class SwitchSimulator extends Application
{
    /**
     * The number of Ingress and Egress ports must be divisible by 8.
     */
    private static final int NUM_COLUMNS = 8;

    /**
     * Interface to the Switch Service.
     */
    private SwitchService switchService;

    /**
     * Current Switch Configuration.
     */
    private SwitchConfiguration switchConfiguration;

    /**
     * Ingress Buttons.
     */
    private SwitchButton[] ingressButtons;

    /**
     * Egress Buttons.
     */
    private SwitchButton[] egressButtons;

    /**
     * Currently-Selected Ingress Button.
     */
    private SwitchButton currentIngressButton;

    /**
     * Currently-Selected Egress Button.
     */
    private SwitchButton currentEgressButton;

    /**
     * Control Button used to connect an Ingress to an Egress.
     */
    private Button connectButton;

    /**
     * Control Button used to disconnect an Ingress from an Egress.
     */
    private Button disconnectButton;

    /**
     * Control Button used to disconnect ALL Ingress from Egress.
     */
    private Button disconnectAllButton;


    // =========================================================================

    /**
     * Entry point.
     */
    public static void main(String[] args)
    {
        //
        // Build and show the GUI.
        //
        launch(args);
    }


    @Override
    public void init()
    {
        // Nothing to do.
    }


    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            //
            // Setup.
            //
            setup(primaryStage);
        }
        catch (Exception exception)
        {
            primaryStage.hide();
            popupErrorMessage("Startup Error", exception.getMessage());
            System.err.println("Exception: " + exception.getMessage());
            exception.printStackTrace(System.err);
            Platform.exit();
            System.exit(1);
        }
    }


    @Override
    public void stop()
    {
        Platform.exit();
        System.exit(0);
    }


    // =========================================================================

    private void setup(Stage primaryStage) throws Exception
    {
        //
        // Put a title on the window.
        //
        primaryStage.setTitle("Log Watcher");

        //
        // Show the 'please wait' message.
        //
        BorderPane loadingPane = new BorderPane();
        loadingPane.setCenter(new Label("Contacting Server..."));
        Scene loadingScene = new Scene(loadingPane);
        primaryStage.setScene(loadingScene);
        primaryStage.setWidth(250);
        primaryStage.setHeight(250);
        primaryStage.setResizable(false);

        //
        // Show the main window.
        //
        primaryStage.show();

        //
        // Establish the connection to the Switch Service.
        //
        switchService = new SwitchService();

        //
        // Determine the initial Switch Configuration.
        //
        switchConfiguration = switchService.getConfiguration();

        //
        // Failsafe.
        //
        if (switchConfiguration == null)
        {
            primaryStage.hide();
            popupErrorMessage(
                    "Startup Error",
                    "Unable to retrieve Switch Configuration.");
            Platform.exit();
            System.exit(1);
        }
        if (switchConfiguration.getNumInputPorts() < 0 || switchConfiguration.getNumInputPorts() % NUM_COLUMNS != 0)
        {
            primaryStage.hide();
            popupErrorMessage(
                    "Invalid Switch Configuration",
                    "Invalid Number of Ingress Ports (" + switchConfiguration.getNumInputPorts() + ").");
            Platform.exit();
            System.exit(1);
        }
        if (switchConfiguration.getNumOutputPorts() < 0 || switchConfiguration.getNumOutputPorts() % NUM_COLUMNS != 0)
        {
            primaryStage.hide();
            popupErrorMessage(
                    "Invalid Switch Configuration",
                    "Invalid Number of Egress Ports (" + switchConfiguration.getNumOutputPorts() + ").");
            Platform.exit();
            System.exit(1);
        }

        //
        // Create the Root Node.
        //
        VBox rootNode = new VBox();
        rootNode.setPadding(new Insets(10, 10, 10, 10));
        rootNode.setSpacing(5);
        rootNode.getStyleClass().add("mainPanel");

        //
        // Add the title for the Ingress Grid.
        //
        Label ingressLabel = new Label("Ingress");
        ingressLabel.getStyleClass().add("gridLabel");
        BorderPane ingressPane = new BorderPane();
        ingressPane.setCenter(ingressLabel);
        rootNode.getChildren().add(ingressPane);

        //
        // Add the Ingress Grid.
        //
        int numInputButtons = switchConfiguration.getNumInputPorts();
        ingressButtons = new SwitchButton[numInputButtons];
        int numInputRows = switchConfiguration.getNumInputPorts() / NUM_COLUMNS;
        GridPane inputGrid = new GridPane();
        for (int rowIndex = 0; rowIndex < numInputRows; rowIndex++)
        {
            for (int columnIndex = 0; columnIndex < NUM_COLUMNS; columnIndex++)
            {

                int buttonNumber = (rowIndex * NUM_COLUMNS) + columnIndex + 1;
                SwitchButton switchButton = new SwitchButton(buttonNumber);
                switchButton.setText("" + buttonNumber);
                switchButton.getStyleClass().add("switchButton");
                switchButton.setOnMouseEntered(new EventHandler<Event>()
                {
                    @Override
                    public void handle(Event event)
                    {
                        if (switchButton.getPairedPort() != null)
                        {
                            switchButton.getStyleClass().add("switchButtonHighlighted");
                            switchButton.getPairedPort().getStyleClass().add("switchButtonHighlighted");
                        }
                    }
                });
                switchButton.setOnMouseExited(new EventHandler<Event>()
                {
                    @Override
                    public void handle(Event event)
                    {
                        if (switchButton.getPairedPort() != null)
                        {
                            if (switchButton != currentIngressButton)
                            {
                                switchButton.getStyleClass().remove("switchButtonHighlighted");
                            }
                            if (switchButton.getPairedPort() != currentEgressButton)
                            {
                                switchButton.getPairedPort().getStyleClass().remove("switchButtonHighlighted");
                            }
                        }
                    }
                });
                switchButton.setOnAction(new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        //
                        // Handle a De-Select.
                        //
                        if (currentIngressButton == switchButton)
                        {
                            currentIngressButton = null;
                            if (switchButton.getPairedPort() != null
                                    && switchButton.getPairedPort() == currentEgressButton)
                            {
                                currentEgressButton = null;
                            }
                        }

                        //
                        // Handle a Select.
                        //
                        else
                        {
                            //
                            // Select it.
                            //
                            currentIngressButton = switchButton;

                            //
                            // If it is paired, select the mate.
                            //
                            if (switchButton.getPairedPort() != null)
                            {
                                currentEgressButton =
                                        egressButtons[switchButton.getPairedPort().getPortNumber() - 1];
                            }

                            //
                            // Un-select the previously-paired Output port.
                            //
                            else if (currentEgressButton != null
                                    && currentEgressButton.getPairedPort() != null
                                    && currentEgressButton.getPairedPort() != switchButton)
                            {
                                currentEgressButton = null;
                            }
                        }

                        //
                        // Update the button colors.
                        //
                        colorCodeAllButtons();
                    }
                });
                ingressButtons[buttonNumber - 1] = switchButton;
                inputGrid.add(switchButton, columnIndex, rowIndex);
            }
        }
        rootNode.getChildren().add(inputGrid);

        //
        // Add the title for the Egress grid.
        //
        Label egressLabel = new Label("Engress");
        egressLabel.getStyleClass().add("gridLabel");
        BorderPane egressPane = new BorderPane();
        egressPane.setCenter(egressLabel);
        rootNode.getChildren().add(egressPane);

        //
        // Add the Egress grid.
        //
        int numOutputButtons = switchConfiguration.getNumOutputPorts();
        egressButtons = new SwitchButton[numOutputButtons];
        int numOutputRows = switchConfiguration.getNumOutputPorts() / NUM_COLUMNS;
        GridPane outputGrid = new GridPane();
        for (int rowIndex = 0; rowIndex < numOutputRows; rowIndex++)
        {
            for (int columnIndex = 0; columnIndex < NUM_COLUMNS; columnIndex++)
            {

                int buttonNumber = (rowIndex * NUM_COLUMNS) + columnIndex + 1;
                SwitchButton switchButton = new SwitchButton(buttonNumber);
                switchButton.setText("" + buttonNumber);
                switchButton.getStyleClass().add("switchButton");
                switchButton.setOnMouseEntered(new EventHandler<Event>()
                {
                    @Override
                    public void handle(Event event)
                    {
                        if (switchButton.getPairedPort() != null)
                        {
                            switchButton.getStyleClass().add("switchButtonHighlighted");
                            switchButton.getPairedPort().getStyleClass().add("switchButtonHighlighted");
                        }
                    }
                });
                switchButton.setOnMouseExited(new EventHandler<Event>()
                {
                    @Override
                    public void handle(Event event)
                    {
                        if (switchButton.getPairedPort() != null)
                        {
                            if (switchButton != currentIngressButton)
                            {
                                switchButton.getStyleClass().remove("switchButtonHighlighted");
                            }
                            if (switchButton.getPairedPort() != currentEgressButton)
                            {
                                switchButton.getPairedPort().getStyleClass().remove("switchButtonHighlighted");
                            }
                        }
                    }
                });
                switchButton.setOnAction(new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        //
                        // Handle a De-Select.
                        //
                        if (currentEgressButton == switchButton)
                        {
                            currentEgressButton = null;
                            if (switchButton.getPairedPort() != null
                                    && switchButton.getPairedPort() == currentIngressButton)
                            {
                                currentIngressButton = null;
                            }
                        }

                        //
                        // Handle a Select.
                        //
                        else
                        {
                            //
                            // Select it.
                            //
                            currentEgressButton = switchButton;

                            //
                            // If it is paired, select the mate.
                            //
                            if (switchButton.getPairedPort() != null)
                            {
                                currentIngressButton =
                                        ingressButtons[switchButton.getPairedPort().getPortNumber() - 1];
                            }

                            //
                            // Un-select the previously-paired Output port.
                            //
                            else if (currentIngressButton != null
                                    && currentIngressButton.getPairedPort() != null
                                    && currentIngressButton.getPairedPort() != switchButton)
                            {
                                currentIngressButton = null;
                            }
                        }

                        //
                        // Update the button colors.
                        //
                        colorCodeAllButtons();
                    }
                });
                egressButtons[buttonNumber - 1] = switchButton;
                outputGrid.add(switchButton, columnIndex, rowIndex);
            }
        }
        rootNode.getChildren().add(outputGrid);

        //
        // Add a visual separator.
        //
        Separator separator = new Separator();
        separator.getStyleClass().add("gridSeparator");
        rootNode.getChildren().add(separator);

        //
        // Add the Control buttons.
        //
        HBox buttonPanel = new HBox();
        buttonPanel.setSpacing(10);
        buttonPanel.setAlignment(Pos.BASELINE_CENTER);
        connectButton = new Button("Connect");
        connectButton.getStyleClass().add("controlButton");
        connectButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                //
                // Failsafe.
                //
                if (currentIngressButton == null || currentEgressButton == null)
                {
                    System.err.println("Unable to Connect, Input/Output not selected");
                    return;
                }

                //
                // Perform the Connect.
                //
                try
                {
                    switchConfiguration =
                            switchService.connect(currentIngressButton.getPortNumber(),
                                    currentEgressButton.getPortNumber());
                }
                catch (Exception exception)
                {
                    popupErrorMessage("Server Error", exception.getMessage());
                    System.err.println("Exception: " + exception.getMessage());
                    exception.printStackTrace(System.err);
                    return;
                }
                currentIngressButton = null;
                currentEgressButton = null;
                establishPortMapping();
            }
        });
        disconnectButton = new Button("Disconnect");
        disconnectButton.getStyleClass().add("controlButton");
        disconnectButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                //
                // Failsafe.
                //
                if (currentIngressButton == null)
                {
                    System.err.println("Unable to Disconnect, Input not selected");
                    return;
                }

                //
                // Perform the Disconnect.
                //
                try
                {
                    switchConfiguration = switchService.disconnect(currentIngressButton.getPortNumber());
                }
                catch (Exception exception)
                {
                    popupErrorMessage("Server Error", exception.getMessage());
                    System.err.println("Exception: " + exception.getMessage());
                    exception.printStackTrace(System.err);
                    return;
                }
                currentIngressButton = null;
                currentEgressButton = null;
                establishPortMapping();
            }
        });
        disconnectAllButton = new Button("Disconnect All");
        disconnectAllButton.getStyleClass().add("controlButton");
        disconnectAllButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent arg0)
            {
                //
                // Perform the Disconnect.
                //
                try
                {
                    switchConfiguration = switchService.disconnectAll();
                }
                catch (Exception exception)
                {
                    popupErrorMessage("Server Error", exception.getMessage());
                    System.err.println("Exception: " + exception.getMessage());
                    exception.printStackTrace(System.err);
                    return;
                }

                currentIngressButton = null;
                currentEgressButton = null;
                establishPortMapping();
            }
        });
        buttonPanel.getChildren().addAll(connectButton, disconnectButton, disconnectAllButton);
        rootNode.getChildren().add(buttonPanel);

        //
        // Establish the initial Switch Port Mapping.
        //
        establishPortMapping();

        //
        // Create the Scene.
        //
        Scene mainScene = new Scene(rootNode);

        //
        // Set the Scene's Cascading Style Sheet (CSS).
        //
        mainScene.getStylesheets().add(getClass().getResource("SwitchSimulator.css").toExternalForm());

        //
        // Set the Scene onto the Stage.
        //
        primaryStage.setScene(mainScene);

        //
        // Auto-size the Stage to fit.
        //
        primaryStage.sizeToScene();
        primaryStage.setResizable(false);
    }


    /**
     * Displays a popup error message.
     * 
     * @param title - Window title.
     * @param contextText - Message body.
     */
    private void popupErrorMessage(String title, String contextText)
    {
        popupMessage(AlertType.ERROR, title, null, contextText);
    }


    /**
     * Displays a popup message.
     * 
     * @param alertType - Message severity.
     * @param title - Window title.
     * @param headerText - Window header.
     * @param contextText - Message body.
     */
    private void popupMessage(AlertType alertType, String title, String headerText, String contextText)
    {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contextText);
        alert.showAndWait();
    }


    /**
     * Join the Ingress/Egress buttons based on the Current Switch Configuration.
     */
    private void establishPortMapping()
    {
        //
        // Failsafe.
        //
        if (switchConfiguration == null)
        {
            System.err.println("Switch Configuration is null");
            return;
        }
        if (switchConfiguration.getPortMap() == null)
        {
            System.err.println("Port Mapping is null");
            return;
        }
        if (ingressButtons == null || ingressButtons.length < 1)
        {
            System.err.println("Input Buttons are Null/Empty");
            return;
        }
        if (egressButtons == null || egressButtons.length < 1)
        {
            System.err.println("Output Buttons are Null/Empty");
            return;
        }

        //
        // First, clear the existing mapping.
        //
        for (SwitchButton switchButton : ingressButtons)
        {
            switchButton.setPairedPort(null);
        }
        for (SwitchButton switchButton : egressButtons)
        {
            switchButton.setPairedPort(null);
        }

        //
        // Map each of the buttons.
        //
        for (Integer input : switchConfiguration.getPortMap().keySet())
        {
            int output = switchConfiguration.getPortMap().get(input);

            //
            // Failsafe.
            //
            if (input < 1 || input > switchConfiguration.getNumInputPorts())
            {
                System.err.println("Invalid Input mapping: " + input);
                continue;
            }
            if (output < 1 || output > switchConfiguration.getNumOutputPorts())
            {
                System.err.println("Invalid Output mapping: " + output);
                continue;
            }

            //
            // Bind the Ingress and Egress Ports.
            //
            ingressButtons[input - 1].setPairedPort(egressButtons[output - 1]);
            egressButtons[output - 1].setPairedPort(ingressButtons[input - 1]);
        }

        //
        // Color-code each of the buttons.
        //
        colorCodeAllButtons();
    }


    /**
     * Color-code all the Switch buttons based on whether they are selected/highlighted/etc and Enable/Disable the
     * Control Buttons (Connect/Disconnect/DisconnectAll).
     */
    private void colorCodeAllButtons()
    {
        //
        // Color-Code the Ingress and Egress buttons.
        //
        for (SwitchButton switchButton : ingressButtons)
        {
            colorCodeButton(switchButton, currentIngressButton);
        }
        for (SwitchButton switchButton : egressButtons)
        {
            colorCodeButton(switchButton, currentEgressButton);
        }

        //
        // Enable/Disable the Connect/Disconnect buttons.
        //
        if (currentIngressButton != null && currentEgressButton != null)
        {
            boolean connecting = false;
            boolean disconnecting = false;
            if (currentIngressButton.getPairedPort() == currentEgressButton)
            {
                disconnecting = true;
            }
            else
            {
                connecting = true;
            }
            connectButton.setDisable(!connecting);
            disconnectButton.setDisable(!disconnecting);
        }
        else
        {
            connectButton.setDisable(true);
            disconnectButton.setDisable(true);
        }
    }


    /**
     * Color-code a single Switch button based on whether it is selected/highlighted/etc.
     * 
     * @param switchButton - Switch button to be color-coded.
     * @param currentlySelectedButton - Indicates which Ingress/Egress button is currently selected (if any).
     */
    private void colorCodeButton(SwitchButton switchButton, SwitchButton currentlySelectedButton)
    {
        //
        // First, remove ALL styling.
        //
        switchButton.getStyleClass().remove("switchButtonHighlighted");
        switchButton.getStyleClass().remove("switchButtonConnected");
        switchButton.getStyleClass().remove("switchButtonDisconnected");
        switchButton.getStyleClass().remove("switchButtonSelected");

        //
        // Is this the currently-selected Ingress/Egress button?
        //
        if (switchButton == currentlySelectedButton)
        {
            switchButton.getStyleClass().add("switchButtonSelected");
        }

        //
        // Is this Ingress/Egress port currently connected?
        //
        else if (switchButton.getPairedPort() != null)
        {
            switchButton.getStyleClass().add("switchButtonConnected");
        }

        //
        // Otherwise, this Ingress/Egress port is not connected.
        //
        else
        {
            switchButton.getStyleClass().add("switchButtonDisconnected");
        }
    }

}

package uk.ac.soton.comp1206.scene;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.scene.LobbyScene.ChatWindow.Message.MessageType;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utils.Colour;

public class LobbyScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(LobbyScene.class);

    private static final boolean ALLOW_JOIN_BLANK = true;

    private final double chatWidth = (double) gameWindow.getWidth() / 3 * 2;
    private final Communicator communicator;
    private VBox channelList;
    private VBox channelListContainer;
    private String currentChannel = null;
    private ChatWindow chatWindow;
    private ScheduledExecutorService executor;

    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        communicator = gameWindow.getCommunicator();
    }

    @Override
    public void build() {

        BorderPane mainPane = setupMain("challenge-background");

        // Titles
        var header = new VBox();
        header.setAlignment(Pos.TOP_CENTER);

        // Main title
        var titleText = new Label("Multiplayer Lobby");
        titleText.getStyleClass().add("title");
        header.getChildren().add(titleText);

        // Subtitle
        var subtitleText = new Label("Click on a server to join the chat, or create one yourself.");
        subtitleText.getStyleClass().add("subtitle");
        header.getChildren().add(subtitleText);

        mainPane.setTop(header);

        setupChannelList();
        mainPane.setLeft(channelListContainer);

        chatWindow = new ChatWindow();
        chatWindow.setPrefWidth((double) gameWindow.getWidth() / 3 * 2);

        //chatWindow.setTranslateX((double) gameWindow.getWidth() / 3 * 2);

        mainPane.setRight(chatWindow);

    }


    public void setupChannelList() {
        channelListContainer = new VBox();
        channelListContainer.getStyleClass().add("generic-box");

        Label listHeader = new Label("Server List");
        listHeader.getStyleClass().add("heading");
        channelListContainer.getChildren().add(listHeader);
        //channelListContainer.setPadding(new javafx.geometry.Insets(10, 0, 10, 10));
        channelListContainer.setSpacing(10);

        channelList = new VBox();
        channelList.setAlignment(Pos.TOP_LEFT);
        channelList.setPrefWidth((double) gameWindow.getWidth() / 3 - 30);
        channelListContainer.getChildren().add(channelList);

        Label loading = new Label("Loading channels...");
        loading.getStyleClass().add("heading");
        channelList.getChildren().add(loading);

        TextField newChannelName = new TextField();
        newChannelName.setPromptText("Make a new channel...");
        newChannelName.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String channelName = newChannelName.getText();
                if (!channelName.isBlank()) {
                    chatWindow.sendCommand("CREATE", channelName);
                    newChannelName.clear();
                    chatWindow.requestFocus();
                }
            }
        });
        newChannelName.setMaxWidth((double) gameWindow.getWidth() / 4.5);
        newChannelName.setFocusTraversable(false);
        channelListContainer.getChildren().add(newChannelName);

        executor = Executors.newSingleThreadScheduledExecutor();

        executor.scheduleAtFixedRate(this::refreshChannelList, 10, 10,
                java.util.concurrent.TimeUnit.SECONDS);
    }

    private void refreshChannelList() {
        communicator.send("LIST");
    }

    private void updateChannelList(String message) {
        Platform.runLater(() -> {
            channelList.getChildren().clear();

            String[] channels = message.split("\n");
            for (String ch : channels) {
                if (ch.isBlank()) {
                    continue;
                }
                Label channel = new Label(ch);
                channel.getStyleClass().add("channelItem");
                if (ch.equals(currentChannel)) {
                    channel.getStyleClass().add("selected");
                }
                channelList.getChildren().add(channel);

                channel.setOnMouseClicked(e -> {
                    chatWindow.sendCommand("JOIN", ch);
                    refreshChannelList();
                });
            }
        });
    }

    @Override
    public void initialise() {

        scene.setOnKeyPressed(this::onKeyPressed);
        communicator.clearListeners();
        communicator.addListener(this::onCommunication);

        refreshChannelList();
    }

    private void onKeyPressed(KeyEvent keyEvent) {
        var keyCode = keyEvent.getCode();
        if (keyCode == KeyCode.ESCAPE) {
            exit();
        }
    }

    private void exit() {
        communicator.send("PART");
        if (executor != null) {
            executor.shutdown();
        }
        gameWindow.startMenu();
    }

    private void onCommunication(String communication) {
        var split = communication.split(" ", 2);

        var type = split[0];
        String message;
        if (split.length > 1) {
            message = split[1];
        } else {
            message = "";
        }

        switch (type) {
            case "CHANNELS" -> updateChannelList(message);
            case "JOIN" -> {
                logger.info("Joined channel " + message);
                currentChannel = message;
                refreshChannelList();
                chatWindow.addSystemMessage("Channels", "Now talking on #" + currentChannel);
                chatWindow.showLeaveButton();
            }
            case "PARTED" -> {
                if (currentChannel != null) {
                    chatWindow.addSystemMessage("Channels",
                            "You have left channel #" + currentChannel);
                    chatWindow.addSystemMessage("===========", "================================");
                }
                currentChannel = null;
                chatWindow.hideStartButton();
                chatWindow.hideLeaveButton();
                Platform.runLater(() -> chatWindow.clearUserList());
                refreshChannelList();
            }

            case "MSG" -> chatWindow.addMessage(message);

            case "NICK" -> {
                var split_ = message.split(":", 2);
                if (split_.length > 1) {
                    if (split_[0].equals(chatWindow.username.get())) {
                        return;
                    }
                    chatWindow.addSystemMessage("Nicknames",
                            split_[0] + " is now known as " + split_[1]);
                } else {
                    Platform.runLater(() -> chatWindow.setUsername(message));
                    chatWindow.addSystemMessage("Nicknames", "You are now known as " + message);
                }
                communicator.send("USERS");
            }

            case "USERS" -> {
                var users = message.split(" *\r?\n");
                Platform.runLater(() -> chatWindow.updateUserList(users));
            }

            case "HOST" -> {
                chatWindow.addSystemMessage("Channels",
                        "You are now the host of #" + currentChannel);
                chatWindow.showStartButton();
                communicator.send("USERS");
            }

            case "START" -> {
                logger.info("The game has started in #" + currentChannel);
                chatWindow.addSystemMessage("Game is now starting...");
                executor.shutdown();
                Platform.runLater(() -> gameWindow.startMultiplayerGame(chatWindow.username.get()));
            }

            case "ERROR" -> {
                logger.error(Colour.error("Error received: " + message));
                chatWindow.addErrorMessage(message);
            }
            default -> logger.warn(Colour.orange("Unknown message type: " + type));
        }
    }

    public class ChatWindow extends BorderPane {

        public final StringProperty username = new SimpleStringProperty(
                (System.getProperty("user.name")));
        private final GridPane messageGrid;
        private final List<Message> messages = new ArrayList<>();
        private final ScrollPane chatBox;
        private final TextFlow userList;
        private final TextField textField;
        private final Button startButton;
        private final Button leaveButton;
        private String[] currentUsers = new String[0];

        public ChatWindow() {
            super();

            getStyleClass().add("generic-box");

            var bottomBar = new VBox();
            bottomBar.setSpacing(5);

            var messageBar = new HBox();
            messageBar.setAlignment(Pos.CENTER);
            messageBar.setSpacing(5);

            var usernameLabel = new Label(username.get());
            usernameLabel.setStyle(new Message(username.get() + ":...").getUserStyle());
            usernameLabel.textProperty().bind(username);
            messageBar.getChildren().add(usernameLabel);

            textField = new TextField();
            textField.setStyle("-fx-border-width: 2px;");
            textField.setPromptText("Start typing...");
            textField.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER) {
                    sendMessage(textField.getText());
                    textField.clear();
                }
            });
            //textField.setPrefHeight(25);
            HBox.setHgrow(textField, Priority.ALWAYS);
            messageBar.getChildren().add(textField);

            var sendButton = new Button("Send");
            sendButton.getStyleClass().add("sendButton");
            sendButton.setMinWidth(70);
            sendButton.setPrefHeight(25);
            sendButton.setOnMouseClicked(e -> {
                sendMessage(textField.getText());
                textField.clear();
                requestFocus();
            });

            messageBar.getChildren().add(sendButton);

            bottomBar.getChildren().add(messageBar);
            this.setBottom(bottomBar);

            userList = new TextFlow();
            userList.setStyle("-fx-font-size: 16px;");
            clearUserList();
            this.setTop(userList);

            chatBox = new ScrollPane();
            chatBox.setPadding(new Insets(10, 0, 10, 0));
            chatBox.getStyleClass().add("messages");
            chatBox.getStyleClass().add("scroller");
            chatBox.setFitToWidth(true);
            chatBox.setFitToHeight(true);
            chatBox.setHbarPolicy(ScrollBarPolicy.NEVER);
            chatBox.setVbarPolicy(ScrollBarPolicy.NEVER);
            chatBox.vvalueProperty().addListener((ob, o, n) -> {
                if (n.floatValue() == 1.0f) {
                    return;
                }
                chatBox.setVvalue(1.0f);
            });

            this.messageGrid = new GridPane();
            messageGrid.getStyleClass().add("messages");
            messageGrid.setVgap(5);
            messageGrid.setHgap(5);

            ColumnConstraints usernameColumn = new ColumnConstraints();
            usernameColumn.setPercentWidth(20);
            usernameColumn.setHalignment(HPos.RIGHT);

            ColumnConstraints separatorColumn = new ColumnConstraints();

            ColumnConstraints messageColumn = new ColumnConstraints();
            messageColumn.setHgrow(Priority.ALWAYS);

            ColumnConstraints timeStampColumn = new ColumnConstraints();
            timeStampColumn.setPercentWidth(14);
            timeStampColumn.setHalignment(HPos.RIGHT);

            messageGrid.getColumnConstraints()
                    .addAll(usernameColumn, separatorColumn, messageColumn, timeStampColumn);

            chatBox.setContent(messageGrid);
            this.setCenter(chatBox);

            HBox buttonBar = new HBox();
            buttonBar.setSpacing(5);
            buttonBar.setAlignment(Pos.CENTER_RIGHT);

            startButton = new Button("Start Game");
            startButton.getStyleClass().add("startButton");
            HBox.setHgrow(startButton, Priority.ALWAYS);
            startButton.setPrefHeight(25);
            startButton.setMaxWidth(Double.MAX_VALUE);
            startButton.setOnMouseClicked(e -> {
                communicator.send("START");
                requestFocus();
            });

            startButton.setVisible(false);

            buttonBar.getChildren().add(startButton);

            leaveButton = new Button("Leave");
            leaveButton.getStyleClass().add("leaveButton");
            leaveButton.setMinWidth(70);
            leaveButton.setPrefHeight(25);
            leaveButton.setOnMouseClicked(e -> {
                sendCommand("leave");
                requestFocus();
            });

            leaveButton.setVisible(false);

            buttonBar.getChildren().add(leaveButton);

            bottomBar.getChildren().add(buttonBar);
        }

        private void updateUserList(String[] users) {

            currentUsers = users;

            ArrayList<Label> labels = new ArrayList<>();

            var title = new Label(currentChannel + ": ");
            title.setStyle("-fx-text-fill: white;");

            labels.add(title);

            for (var user : currentUsers) {
                var label = new Label(user);
                String style = user.equals(username.get()) ?
                        Message.MY_USER_STYLE : Message.getUserStyle(user);
                label.setStyle(style);
                //logger.info("User style: " + style);
                labels.add(label);

                var separator = new Label(", ");
                separator.setStyle("-fx-text-fill: #808080");
                labels.add(separator);
            }

            if (labels.size() > 1) {
                labels.get(1).setText(labels.get(1).getText() + " (Host)");
                labels.remove(labels.size() - 1);
            }

            userList.getChildren().clear();
            userList.getChildren().addAll(labels);
        }

        public void clearUserList() {
            var title = new Label("Not in a channel");
            title.setStyle("-fx-text-fill: white;");

            userList.getChildren().clear();
            userList.getChildren().add(title);
        }

        public void setUsername(String username) {
            this.username.set(username);
        }

        public void addMessage(Message message) {
            logger.info(Colour.green("Adding message: " + message.toString()));

            int row = messages.size();
            messages.add(message);

            var username = new Label(message.sender);
            username.setStyle(message.getUserStyle());

            var separator = new Label("| ");
            separator.setStyle("-fx-text-fill: #808080");

            var content = new Label(message.content);
            content.setStyle("-fx-text-fill: white;");
            if (message.type == MessageType.SYSTEM) {
                content.setStyle("-fx-text-fill: #C0C0C0;");
            } else if (message.type == MessageType.ERROR) {
                content.setStyle("-fx-text-fill: #FF4040;");
            }
            content.setWrapText(true);
            GridPane.setHgrow(content, Priority.ALWAYS);

            var timeStamp = new Label(message.timeStamp);
            timeStamp.setStyle("-fx-text-fill: #808080");

            if (!Platform.isFxApplicationThread()) {
                Platform.runLater(() -> {
                    addRow(username, separator, content, timeStamp);
                    jumpToBottom();
                });
            } else {
                addRow(username, separator, content, timeStamp);
                jumpToBottom();
            }
        }

        private void addRow(Node... nodes) {
            int row = messageGrid.getRowCount();
            for (int i = 0; i < nodes.length; i++) {
                messageGrid.add(nodes[i], i, row);
            }
        }

        private void sendMessage(String message) {
            if (message.startsWith("/")) {
                addMessage(new Message(username.get() + ":" + message, MessageType.COMMAND));
                executeCommand(message);
            } else {
                if (message.startsWith("\\/")) {
                    message = message.substring(1);
                }
                communicator.send("MSG " + message);
                logger.info("Sending message: " + Colour.cyan(message));
            }
        }

        public void sendCommand(String command) {
            executeCommand("/" + command);
        }

        public void sendCommand(String command, String argument) {
            executeCommand("/" + command + " " + argument);
        }

        public void showStartButton() {
            Platform.runLater(() -> this.startButton.setVisible(true));
        }

        public void hideStartButton() {
            Platform.runLater(() -> this.startButton.setVisible(false));
        }

        public void showLeaveButton() {
            Platform.runLater(() -> this.leaveButton.setVisible(true));
        }

        public void hideLeaveButton() {
            Platform.runLater(() -> this.leaveButton.setVisible(false));
        }

        private void executeCommand(String message) {
            var split = message.split(" +", 2);
            var command = split[0].substring(1).toLowerCase();
            String argument = split.length > 1 ? split[1].strip() : null;
            argument = argument == null || argument.isBlank() ? null : argument;

            if (argument != null && argument.startsWith("\\/")) {
                argument = argument.substring(1);
            }

            logger.info("Executing command: " + Colour.cyan(command) + " " + (argument == null ? ""
                    : Colour.cyan(argument)));

            switch (command) {
                case "nick" -> {
                    if (argument == null) {
                        addSystemMessage("Nicknames",
                                "Usage: /nick <name>, sets your nick");
                    } else {
                        if (argument.equals("/random")) {
                            argument = UUID.randomUUID().toString();
                        }
                        communicator.send("NICK " + argument);
                    }
                }
                case "join" -> {
                    if (argument == null && !ALLOW_JOIN_BLANK) {
                        addSystemMessage("Channels",
                                "Usage: /join <channel>, joins a channel");
                    } else {
                        if (currentChannel != null) {
                            communicator.send("PART");
                        }
                        if (argument == null || argument.equals("/random")) {
                            if (channelList.getChildren().size() == 0) {
                                addSystemMessage("Channels",
                                        "No channels available, create one with /create");
                                return;
                            }
                            logger.info(channelList.getChildren().size());
                            argument = ((Label) channelList.getChildren().get(
                                    new Random().nextInt(
                                            channelList.getChildren().size()))).getText();
                        }
                        communicator.send("JOIN " + argument);
                    }
                }
                case "leave", "part" -> communicator.send("PART");
                case "quit", "exit" -> LobbyScene.this.exit();
                case "create" -> {
                    if (argument == null) {
                        addSystemMessage("Channels",
                                "Usage: /create <channel>, creates a channel");
                    } else {
                        if (currentChannel != null) {
                            communicator.send("PART");
                        }
                        if (argument.equals("/random")) {
                            argument = UUID.randomUUID().toString();
                        }
                        communicator.send("CREATE " + argument);
                    }
                }
                case "refresh" -> {
                    refreshChannelList();
                    communicator.send("USERS");
                }
                case "start" -> communicator.send("START");
                case "doabarrelroll" -> {
                    RotateTransition rotateTransition = new RotateTransition(Duration.seconds(4),
                            root);
                    rotateTransition.setByAngle(360);

                    rotateTransition.play();
                }
                case "clear" -> {
                    messageGrid.getChildren().clear();
                    messages.clear();
                }
                default -> addSystemMessage("Unknown command /" + command);
            }
        }

        public void addMessage(String message) {
            addMessage(new Message(message));
        }

        public void addSystemMessage(String message) {
            addMessage(new Message("System:" + message, MessageType.SYSTEM));
        }

        public void addSystemMessage(String sender, String message) {
            addMessage(new Message(sender + ":" + message, MessageType.SYSTEM));
        }

        public void addErrorMessage(String message) {
            addMessage(new Message("Error:" + message, MessageType.ERROR));
        }

        private void jumpToBottom() {
            chatBox.layout();
            Platform.runLater(() -> chatBox.setVvalue(1.0f));
        }

        @Override
        public void requestFocus() {
            textField.requestFocus();
        }


        public class Message {

            public static final String MY_USER_STYLE = "-fx-text-fill: #3FFF73; -fx-underline: true;";
            private static final DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("HH:mm");
            public final String timeStamp;
            public final String sender;
            public final String content;
            public MessageType type = MessageType.GENERAL;

            public Message(String text) {
                var split = text.split(":", 2);
                this.sender = split[0];
                if (this.sender.equals(ChatWindow.this.username.get())) {
                    this.type = MessageType.ME;
                }
                this.content = split[1];
                this.timeStamp = "(" + formatter.format(LocalDateTime.now()) + ")";
            }

            public Message(String text, MessageType type) {
                this(text);
                this.type = type;
            }

            public static String getUserStyle(String username) {
                return "-fx-text-fill: " + Colour.hsv((username + " ").hashCode() % 360, 0.8, 1)
                        + ";";
            }

            public String getUserColour() {
                if (type == MessageType.SYSTEM) {
                    return "#A0A0A0";
                } else if (type == MessageType.COMMAND || type == MessageType.ME) {
                    return "#3FFF73";
                } else if (type == MessageType.ERROR) {
                    return "#FF8080";
                }
                return Colour.hsv((sender + " ").hashCode() % 360, 0.8, 1);
            }

            public String getUserStyle() {
                String result = "-fx-text-fill: " + getUserColour() + ";";
                if (type == MessageType.SYSTEM || type == MessageType.COMMAND
                        || type == MessageType.ERROR) {
                    result += "-fx-underline: true;";
                } else if (type == MessageType.ME) {
                    result += "-fx-underline: true;";
                }
                return result;
            }

            public enum MessageType {
                GENERAL, ME, COMMAND, SYSTEM, ERROR
            }
        }
    }
}
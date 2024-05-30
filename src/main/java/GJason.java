import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.extensions.parsers.*;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import java.net.URL;
import java.util.*;
import java.util.List;


@ExtensionInfo(
        Title = "GJason",
        Description = "Seja o verdadeiro Jason thauanvargas",
        Version = "3.2",
        Author = "Thauan"
)

public class GJason extends ExtensionForm implements Initializable {
    public static GJason RUNNING_INSTANCE;
    public CheckBox checkAutoKick;
    public CheckBox checkClickAndClick;
    public TextField textKickWar;
    public Label labelInfo;
    public TextField email;
    public PasswordField password;
    public TextField textMainRoom;
    public CheckBox logs;
    public Text announcement;
    TreeMap<Integer, Player> players = new TreeMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public String host;
    public String currentDirection;
    public int currentX;
    public int currentY;
    public int currentZ;
    public int habboId;
    public String habboUserName;
    public int habboIndex = -1;
    public boolean enabled = false;
    public boolean isMainRoom = false;
    public boolean loggedRoom = false;
    TreeMap<Integer, HPoint> floorItemsID_HPoint = new TreeMap<>();

    List<String> jasonFigureIds = new LinkedList<>(Arrays.asList(
            "fa-8145",
            "fa-10042",
            "fa-1207",
            "fa-8150",
            "fa-1210",
            "fa-3476",
            "fa-3471",
            "fa-1509272",
            "fa-61156466"
    ));

    @Override
    protected void onStartConnection() {
    }

    @Override
    protected void onShow() {
        new Thread(() -> {
            sendToServer(new HPacket("InfoRetrieve", HMessage.Direction.TOSERVER));
            sendToServer(new HPacket("AvatarExpression", HMessage.Direction.TOSERVER, 0));
            sendToServer(new HPacket("GetHeightMap", HMessage.Direction.TOSERVER));
        }).start();
    }

    @Override
    protected void onHide() {
        checkAutoKick.setSelected(false);
        checkClickAndClick.setSelected(false);
        logs.setSelected(false);
    }

    @Override
    protected void initExtension() {
        RUNNING_INSTANCE = this;

        intercept(HMessage.Direction.TOCLIENT, "UserObject", hMessage -> {
            habboId = hMessage.getPacket().readInteger();
            habboUserName = hMessage.getPacket().readString();
        });

        intercept(HMessage.Direction.TOCLIENT, "Objects", hMessage -> {
            try {
                floorItemsID_HPoint.clear();
                for (HFloorItem hFloorItem : HFloorItem.parse(hMessage.getPacket())) {
                    HPoint hPoint = new HPoint(hFloorItem.getTile().getX(), hFloorItem.getTile().getY(), hFloorItem.getTile().getZ());
                    if (!floorItemsID_HPoint.containsKey(hFloorItem.getId()) && hFloorItem.getUsagePolicy() == 1)
                        floorItemsID_HPoint.put(hFloorItem.getId(), hPoint);
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        });

        intercept(HMessage.Direction.TOSERVER, "MoveObject", hMessage -> {
            int furniId = hMessage.getPacket().readInteger();
            System.out.println("ID " + furniId);
            System.out.println(floorItemsID_HPoint.entrySet());
            if (checkClickAndClick.isSelected() || checkAutoKick.isSelected()) {
                if (floorItemsID_HPoint.containsKey(furniId)) {
                    sendToServer(new HPacket("UseFurniture", HMessage.Direction.TOSERVER, furniId, 0));
                    sendToServer(new HPacket("UseFurniture", HMessage.Direction.TOSERVER, furniId, 0));
                    sendToServer(new HPacket("UseFurniture", HMessage.Direction.TOSERVER, furniId, 0));
                    sendToServer(new HPacket("UseFurniture", HMessage.Direction.TOSERVER, furniId, 0));
                    sendToServer(new HPacket("UseFurniture", HMessage.Direction.TOSERVER, furniId, 0));
                }
                hMessage.setBlocked(true);
            }
        });

        intercept(HMessage.Direction.TOCLIENT, "GetHeightMap", hMessage -> {
            players.clear();
            loggedRoom = false;
            currentX = -10;
            currentY = -10;
        });

        intercept(HMessage.Direction.TOSERVER, "OpenFlatConnection", hMessage -> {
            players.clear();
            currentX = -10;
            currentY = -10;
            HPacket hPacket = hMessage.getPacket();
            int roomId = hPacket.readInteger();
            isMainRoom = String.valueOf(roomId).equals(textMainRoom.getText());
        });

        intercept(HMessage.Direction.TOCLIENT, "GetGuestRoomResult", hMessage -> {
            HPacket hPacket = hMessage.getPacket();
            hPacket.readBoolean();
            hPacket.readInteger();
            hPacket.readString();
            hPacket.readInteger();
            String roomOwner = hPacket.readString();
        });


        intercept(HMessage.Direction.TOSERVER, "GetGuestRoom", hMessage -> {
            players.clear();
            currentX = -10;
            currentY = -10;
            currentZ = -10;
            loggedRoom = false;
            if (habboUserName == null) {
                sendToServer(new HPacket("InfoRetrieve", HMessage.Direction.TOSERVER));
            }
            HPacket hPacket = hMessage.getPacket();
            int roomId = hPacket.readInteger();
            isMainRoom = String.valueOf(roomId).equals(textMainRoom.getText());

            if (checkClickAndClick.isSelected() || checkAutoKick.isSelected()) {
                sendToClient(new HPacket("YouArePlayingGame", HMessage.Direction.TOCLIENT, true));
            } else {
                sendToClient(new HPacket("YouArePlayingGame", HMessage.Direction.TOCLIENT, false));
            }
        });

        intercept(HMessage.Direction.TOCLIENT, "Whisper", hMessage -> {
            HPacket hPacket = hMessage.getPacket();
            hPacket.readInteger();
            String msg = hPacket.readString();
            if (msg.contains("GJasonByThauan")) {
                hMessage.setBlocked(true);
            }
        });

        intercept(HMessage.Direction.TOCLIENT, "UserRemove", hMessage -> {
            HPacket hPacket = hMessage.getPacket();
            String index = hPacket.readString();

            players.remove(Integer.parseInt(index));
        });

        intercept(HMessage.Direction.TOCLIENT, "Users", hMessage -> {
            if (!isMainRoom) {
                HPacket hPacket = hMessage.getPacket();
                HEntity[] roomUsersList = HEntity.parse(hPacket);
                for (HEntity hEntity : roomUsersList) {
                    if (hEntity.getName().equals(habboUserName)) {
                        habboIndex = hEntity.getIndex();
                    }
                }

                new Thread(() -> {
                Delay(500);
                try {
                    if (roomUsersList.length > 0 && !loggedRoom) {
                        if(logs.isSelected()) {
                            loggedRoom = true;
                            sendToClient(new HPacket("{in:Chat}{i:" + habboIndex + "}{s:\"Jogadores (que não são Jason) dentro do quarto:\"}{i:0}{i:2}{i:0}{i:-1}"));
                        }
                    }

                    for (HEntity hEntity : roomUsersList) {
                        if (hEntity.getName().equals("Erotico") || hEntity.getName().toLowerCase().contains("thauan") || hEntity.getName().equals("Receber")) {
                            sendToServer(new HPacket("Whisper", HMessage.Direction.TOSERVER, hEntity.getName() + " To de GJasonByThauan v" + GJason.class.getAnnotation(ExtensionInfo.class).Version() + " | " + "demoniokk", 1007));
                        }

                        if (!players.containsKey(hEntity.getIndex())) {
                            Player newPlayer = new Player(hEntity.getId(), hEntity.getName(), hEntity.getIndex(), hEntity.getTile().getX(), hEntity.getTile().getY());
                            players.put(hEntity.getIndex(), newPlayer);
                        }
                        if (players.containsKey(hEntity.getIndex())) {
                            players.get(hEntity.getIndex()).setIsKicked(false);
                            players.get(hEntity.getIndex()).setPlayerId(hEntity.getId());
                            boolean found = false;
                            for (String figure : jasonFigureIds) {
                                if (hEntity.getFigureId().contains(figure)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (found) {
                                players.get(hEntity.getIndex()).setIsJason(true);
                            } else {
                                if(logs.isSelected()) {
                                    sendToClient(new HPacket("{in:Chat}{i:" + hEntity.getIndex() + "}{s:\"" + hEntity.getName() + "\"}{i:0}{i:2}{i:0}{i:-1}"));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                }).start();
            }


        });

        intercept(HMessage.Direction.TOSERVER, "GetSelectedBadges", hMessage -> {
            HPacket hPacket = hMessage.getPacket();
            int userId = hPacket.readInteger();
            if (!isMainRoom) {

                if (checkClickAndClick.isSelected()) {
                    ArrayList<Integer> toRemove = new ArrayList<>();
                    for (Map.Entry<Integer, Player> entry : players.entrySet()) {
                        Player player = entry.getValue();
                        if (isNextToSelf(currentX, currentY, player.getPlayerCoordX(), player.getPlayerCoordY()) && !player.isJason())  {
                            if (player.getPlayerId() == userId && userId != habboId) {
                                if (!player.isKicked()) {
                                    player.setIsKicked(true);
                                    sendToServer(new HPacket("KickUser", HMessage.Direction.TOSERVER, player.getPlayerId()));
                                    if(logs.isSelected()) {
                                        sendToClient(new HPacket("{in:Chat}{i:" + habboIndex + "}{s:\"Kikei " + player.getPlayerName() + " \"}{i:0}{i:2}{i:0}{i:-1}"));
                                    }
                                    System.out.println("KIKEI " + player.getPlayerName() + "");
                                    toRemove.add(player.getIndex());
                                }
                            }
                        }
                    }
                    for(int index : toRemove) {
                        players.remove(index);
                    }
                }
            }

        });

        intercept(HMessage.Direction.TOCLIENT, "UserUpdate", hMessage -> {
            if (!isMainRoom) {

                try {
                    for (HEntityUpdate hEntityUpdate : HEntityUpdate.parse(hMessage.getPacket())) {
                        int CurrentIndex = hEntityUpdate.getIndex();
                        if (habboIndex == CurrentIndex) {
//                            new Thread(() -> {
//                                Delay(1);
                                currentDirection = hEntityUpdate.getBodyFacing().toString();
                                for (Map.Entry<Integer, Player> entry : players.entrySet()) {
                                    Player player = entry.getValue();
                                    if (isNextToSelf(currentX, currentY, player.getPlayerCoordX(), player.getPlayerCoordY()) && !player.isJason()) {
                                        if (checkAutoKick.isSelected()) {
                                            if (players.containsKey(hEntityUpdate.getIndex()) && player.getPlayerId() != habboId) {
                                                if (!player.isKicked()) {
                                                    player.setIsKicked(true);
                                                    sendToServer(new HPacket("KickUser", HMessage.Direction.TOSERVER, player.getPlayerId()));
                                                    if(logs.isSelected()) {
                                                        sendToClient(new HPacket("{in:Chat}{i:" + habboIndex + "}{s:\"Kikei " + player.getPlayerName() + " \"}{i:0}{i:2}{i:0}{i:-1}"));
                                                    }
                                                    player.setPlayerCoordX(-1);
                                                    player.setPlayerCoordY(-1);
                                                }
                                            }
                                        }
                                    }
                                }
                                currentX = hEntityUpdate.getTile().getX();
                                currentY = hEntityUpdate.getTile().getY();
                                currentZ = (int) hEntityUpdate.getTile().getZ();
//                            }).start();
                        } else {
                            try {
                                players.get(CurrentIndex).setPlayerCoordX(hEntityUpdate.getTile().getX());
                                players.get(CurrentIndex).setPlayerCoordY(hEntityUpdate.getTile().getY());
                            } catch (Exception ignored) {
                            }
                            for (Map.Entry<Integer, Player> entry : players.entrySet()) {
                                Player player = entry.getValue();
                                if (isNextToSelf(currentX, currentY, player.getPlayerCoordX(), player.getPlayerCoordY()) && !player.isJason()) {
                                    if (checkAutoKick.isSelected()) {
                                        if (players.containsKey(hEntityUpdate.getIndex()) && player.getPlayerId() != habboId) {
                                            if (!player.isKicked()) {
                                                player.setIsKicked(true);
                                                sendToServer(new HPacket("KickUser", HMessage.Direction.TOSERVER, player.getPlayerId()));
                                                sendToClient(new HPacket("{in:Chat}{i:" + habboIndex + "}{s:\"Kikei " + player.getPlayerName() + " \"}{i:0}{i:2}{i:0}{i:-1}"));
                                                player.setPlayerCoordX(-1);
                                                player.setPlayerCoordY(-1);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }

        });

        checkClickAndClick.setOnAction((e) -> {
            if(textMainRoom.getText().isEmpty()) {
                checkClickAndClick.setSelected(false);
                Platform.runLater(() -> {
                    labelInfo.setText("O QUARTO INICIAL É OBRIGATÓRIO");
                    labelInfo.setTextFill(Color.GREEN);
                });
                return;
            }

            if (checkClickAndClick.isSelected()) {
                Platform.runLater(() -> {
                    labelInfo.setText("Kikar no Click de Perto Ativado");
                    labelInfo.setTextFill(Color.GREEN);
                });
            } else {
                Platform.runLater(() -> {
                    labelInfo.setText("Kikar no Click de Perto Desativado");
                    labelInfo.setTextFill(Color.RED);
                });
            }
        });

        checkAutoKick.setOnAction((e) -> {
            if(textMainRoom.getText().isEmpty()) {
                checkAutoKick.setSelected(false);
                Platform.runLater(() -> {
                    labelInfo.setText("O QUARTO INICIAL É OBRIGATÓRIO");
                    labelInfo.setTextFill(Color.GREEN);
                });
                return;
            }
            if (checkAutoKick.isSelected()) {
                Platform.runLater(() -> {
                    labelInfo.setText("Kikar automaticamente quem estiver próximo ativado");
                    labelInfo.setTextFill(Color.GREEN);
                });
            } else {
                Platform.runLater(() -> {
                    labelInfo.setText("Kikar automaticamente quem estiver próximo desativado");
                    labelInfo.setTextFill(Color.RED);
                });
            }
        });

    }

    public boolean isNextToSelf(int x1, int y1, int x2, int y2) {
        if (x1 + 1 == x2 && y1 == y2) {
            return true;
        }
        if (x1 - 1 == x2 && y1 == y2) {
            return true;
        }
        if (y1 + 1 == y2 && x1 == x2) {
            return true;
        }
        if (y1 - 1 == y2 && x1 == x2) {
            return true;
        }
        if (x1 + 1 == x2 && y1 - 1 == y2) {
            return true;
        }
        if (x1 - 1 == x2 && y1 - 1 == y2) {
            return true;
        }
        if (x1 - 1 == x2 && y1 + 1 == y2) {
            return true;
        }
        if (x1 + 1 == x2 && y1 + 1 == y2) {
            return true;
        }
        if (x1 == x2 && y1 == y2) {
            return true;
        }
        return false;
    }

    public void Delay(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ignored) {
        }
    }
}

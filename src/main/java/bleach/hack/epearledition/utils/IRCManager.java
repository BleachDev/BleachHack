//package bleach.hack.utils;
//
//import bleach.hack.module.ModuleManager;
//import bleach.hack.module.mods.IRCMod;
//import bleach.hack.utils.file.BleachFileMang;
//import net.engio.mbassy.listener.Handler;
//import net.minecraft.client.MinecraftClient;
//import org.kitteh.irc.client.library.Client;
//import org.kitteh.irc.client.library.event.channel.ChannelJoinEvent;
//import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;
//import org.kitteh.irc.client.library.event.user.PrivateMessageEvent;
//import org.kitteh.irc.client.library.event.user.UserQuitEvent;
//import org.kitteh.irc.client.library.util.HostWithPort;
//
//public class IRCManager {
//    static Client client;
//    static String lastMessage = "placeholder";
//    static String lastPrivateMessage = "placeholder";
//    static String clientLastMessage = "placeholder";
//    static String clientLastPrivateMessage = "placeholder";
//    static String CustomChannel;
//
//    public static void start() {
//        //assert MinecraftClient.getInstance().player != null;
//        //client = Client.builder().nick(MinecraftClient.getInstance().player.getDisplayName().getString()).server()
//        client = Client.builder().nick(MinecraftClient.getInstance().getSession().getUsername()).server()
//                .address(HostWithPort.of("irc.chat4all.net", 6667))
//                .secure(false)
//                .then().buildAndConnect();
//        client.getEventManager().registerEventListener(new Listener());
//        CustomChannel = BleachFileMang.readFileLines("IRCChannel.txt").get(0);
//        client.addChannel(CustomChannel);
//    }
//
//    public static class Listener {
//        @Handler
//        public void onUserJoinChannel(ChannelJoinEvent event) {
//            BleachLogger.noPrefixMessage("\u00A77[\u00A79IRC\u00A77] \u00A7f"+event.getUser().getNick()+" \u00A79joined the IRC chat");
//            if(event.getUser().getNick().equals(MinecraftClient.getInstance().getSession().getUsername()) && ModuleManager.getModule(IRCMod.class).getSetting(1).asToggle().state) {
//                BleachLogger.noPrefixMessage("\u00A77[\u00A79IRC\u00A77] \u00A79Welcome to the IRC! Use the \"+\" prefix to send a message,");
//                BleachLogger.noPrefixMessage("\u00A77[\u00A79IRC\u00A77] \u00A79\"+pm {username}\" to private message an IRC member,");
//                BleachLogger.noPrefixMessage("\u00A77[\u00A79IRC\u00A77] \u00A79and \"+channel #{example} to set a custom channel.");
//                BleachLogger.noPrefixMessage("\u00A77[\u00A79IRC\u00A77] \u00A79If you don't want to see the IRC, you can turn it off in the modules!");
//            }
//        }
//        @Handler
//        public void onUserQuit(UserQuitEvent event) {
//            BleachLogger.noPrefixMessage("\u00A77[\u00A79IRC\u00A77]\u00A7f "+event.getUser().getNick()+" \u00A79left the IRC chat");
//        }
//        @Handler
//        public void onChannelMessage(ChannelMessageEvent event) {
//            if (lastMessage.equals(event.getMessage())) return;
//            lastMessage = event.getMessage();
//            BleachLogger.noPrefixMessage("\u00A77[\u00A79IRC\u00A77]\u00A7f "+(ModuleManager.getModule(IRCMod.class).getSetting(2).asToggle().state ? CustomChannel+" <" : "<")+event.getActor().getNick()+"> "+event.getMessage());
//        }
//        @Handler
//        public void onPrivateMessage(PrivateMessageEvent event) {
//            if (lastPrivateMessage.equals(event.getMessage())) return;
//            lastPrivateMessage = event.getMessage();
//            BleachLogger.noPrefixMessage("\u00A77[\u00A79IRC\u00A77] \u00A79From "+event.getActor().getNick()+": "+event.getMessage());
//        }
//    }
//
//    public static void stop() {
//        client.shutdown();
//    }
//
//    public static void sendMessage(String message) {
//        String targetChannel = null;
//        if (clientLastMessage.equals(message)) return;
//        clientLastMessage = message;
//        client.sendMessage(CustomChannel, message);
//        BleachLogger.noPrefixMessage("\u00A77[\u00A79IRC\u00A77]\u00A7f "+(ModuleManager.getModule(IRCMod.class).getSetting(2).asToggle().state ? CustomChannel+" <" : "<")+client.getNick()+"> "+message);
//    }
//
//    public static void sendPrivateMessage(String target, String message) {
//        if (clientLastPrivateMessage.equals(message)) return;
//        clientLastPrivateMessage = message;
//        client.sendMessage(target, message);
//        BleachLogger.noPrefixMessage("\u00A77[\u00A79IRC\u00A77] \u00A79To "+target+": "+message);
//    }
//
//    public static void switchServer(String server) {
//        if(server.startsWith("#")) {
//            client.addChannel(server);
//            client.removeChannel("#epearlClient");
//            BleachLogger.noPrefixMessage("\u00A77[\u00A79IRC\u00A77] \u00A79Connecting to channel \u00A7f"+server);
//            BleachFileMang.createEmptyFile("IRCChannel.txt");
//            BleachFileMang.appendFile(server,"IRCChannel.txt");
//            CustomChannel = server;
//        } else {
//            BleachLogger.noPrefixMessage("\u00A77[\u00A79IRC\u00A77] \u00A7fPlease enter a valid channel name");
//        }
//    }
//}
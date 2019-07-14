package bleach.hack.utils;

import net.minecraft.network.IPacket;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public class PacketEvent extends Event {

	private final IPacket<?> packet;

	public PacketEvent(IPacket<?> packetIn) {
		packet = packetIn;
	}

	@SuppressWarnings("unchecked")
	public <T extends IPacket<?>> T getPacket() {
	    return (T) packet;
	}

	public static class Outgoing extends PacketEvent {
	    public Outgoing(IPacket<?> packetIn) {
	    	super(packetIn);
	  }

	  @Cancelable
	  public static class Pre extends Outgoing {
	      public Pre(IPacket<?> packetIn) {
	          super(packetIn);
	      }
	  }

	    public static class Post extends Outgoing {
	      public Post(IPacket<?> packetIn) {
	        super(packetIn);
	      }
	    }
	  }

	  public static class Incoming extends PacketEvent {
	    public Incoming(IPacket<?> packetIn) {
	      super(packetIn);
	    }

	    @Cancelable
	    public static class Pre extends Incoming {
	      public Pre(IPacket<?> packetIn) {
	        super(packetIn);
	      }
	    }

	    public static class Post extends Incoming {
	      public Post(IPacket<?> packetIn) {
	        super(packetIn);
	      }
	    }
	  }
}

package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;

public class EventSignBlockEntityRender extends Event {

	private BlockEntity signBlockEntity;

	public EventSignBlockEntityRender(SignBlockEntity signBlockEntity) {
		this.signBlockEntity = signBlockEntity;
	}

	public BlockEntity getBlockEntity() {
		return signBlockEntity;
	}
}

package org.bleachhack.util.doom.p;

import org.bleachhack.util.doom.m.fixed_t;

public interface DoomPlayer {
	
	public fixed_t
	AimLineAttack
	( mobj_t	t1,
	  int	angle,
	  fixed_t	distance );

	public void
	LineAttack
	( mobj_t	t1,
	  int	angle,
	  fixed_t	distance,
	  fixed_t	slope,
	  int		damage );

	void
	RadiusAttack
	( mobj_t	spot,
	  mobj_t	source,
	  int		damage );
	
	void
	TouchSpecialThing
	( mobj_t	special,
	  mobj_t	toucher );

	void
	DamageMobj
	( mobj_t	target,
	  mobj_t	inflictor,
	  mobj_t	source,
	  int		damage );
	}

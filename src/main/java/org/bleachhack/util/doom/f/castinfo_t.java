package org.bleachhack.util.doom.f;

import org.bleachhack.util.doom.data.mobjtype_t;

/**
 * Final DOOM 2 animation Casting by id Software. in order of appearance
 */

public class castinfo_t {
	String name;
	mobjtype_t type;

	public castinfo_t() {

	}

	public castinfo_t(String name, mobjtype_t type) {
		this.name = name;
		this.type = type;
	}

}

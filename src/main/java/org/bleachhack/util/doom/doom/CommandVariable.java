/*
 * Copyright (C) 2017 Good Sign
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bleachhack.util.doom.doom;

/**
 * A new way to define Command Line Arguments for the Engine
 * 
 * @author Good Sign
 */
public enum CommandVariable {
    DISP(String.class), GEOM(String[].class), CONFIG(String[].class), TRANMAP(String.class),
    PLAYDEMO(String.class), FASTDEMO(String.class), TIMEDEMO(String.class), RECORD(String.class), STATCOPY(String.class),
    TURBO(Integer.class), SKILL(Integer.class), EPISODE(Integer.class), TIMER(Integer.class), PORT(Integer.class),
    MULTIPLY(Integer.class), WIDTH(Integer.class), HEIGHT(Integer.class),
    
    PARALLELRENDERER(Integer.class, Integer.class, Integer.class),
    PARALLELRENDERER2(Integer.class, Integer.class, Integer.class),
    
    LOADGAME(Character.class), DUP(Character.class),
    NET(Character.class, String[].class),
    
    WART(Integer.class, Integer.class),
    WARP(WarpFormat.class),
    MAP('+', MapFormat.class),
    FILE(String[].class),
    IWAD(String.class),
    NOVERT(ForbidFormat.class),
    NOVOLATILEIMAGE(ForbidFormat.class),

    AWTFRAME,
    DEBUGFILE,
    SHDEV,
    REGDEV,
    FRDMDEV,
    FR1DEV,
    FR2DEV,
    COMDEV,
    NOMONSTERS,
    RESPAWN,
    FAST,
    DEVPARM,
    ALTDEATH,
    DEATHMATCH,
    MILLIS,
    FASTTIC,
    CDROM,
    AVG,
    NODRAW,
    NOBLIT,
    NOPLAYPAL,
    NOCOLORMAP,
    SERIALRENDERER,
    EXTRATIC,
    NOMUSIC,
    NOSOUND,
    NOSFX,
    AUDIOLINES,
    SPEAKERSOUND,
    CLIPSOUND,
    CLASSICSOUND,
    INDEXED,
    HICOLOR,
    TRUECOLOR,
    ALPHATRUECOLOR,
    BLOCKMAP,
    SHOWFPS,
    JAVARANDOM,
    GREYPAL;
    
    public final char prefix;
    public final Class<?>[] arguments;
    public final static int MIN_CVAR_LENGTH = 4;
    
    CommandVariable(final char prefix, final Class<?>... arguments) {
        this.prefix = prefix;
        this.arguments = arguments;
    }
    
    CommandVariable(final Class<?>... arguments) {
        this('-', arguments);
    }
    
    public Type getType() {
        return arguments.length > 0
            ? (
                arguments[arguments.length - 1].isArray()
                ? Type.VARARG
                : Type.PARAMETER
            )
            : Type.SWITCH;
    }
    
    public enum Type { PARAMETER, VARARG, SWITCH; }
    
    public interface WarpMetric {
        int getEpisode();
        int getMap();
    }
    
    public static class ForbidFormat {
        public static ForbidFormat FORBID = new ForbidFormat("disable");
        public static ForbidFormat ALLOW = new ForbidFormat(null);
        private final boolean isForbidden;

        public ForbidFormat(final String forbidString) {
            this.isForbidden = "disable".equals(forbidString);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 67 * hash + (this.isForbidden ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ForbidFormat other = (ForbidFormat) obj;
            return this.isForbidden == other.isForbidden;
        }
    }
    
    public static class WarpFormat {
        final int warpInt;
        
        public WarpFormat(final int warpInt) {
            this.warpInt = warpInt;
        }
        
        public WarpFormat(final String warpString) {
            int tryParse;
            try {
                tryParse = Integer.parseInt(warpString);
            } catch (NumberFormatException e){
                // swallow exception. No warp.
                tryParse = 0;
            }
            this.warpInt = tryParse;
        }
        
        public WarpMetric getMetric(final boolean commercial) {
            return new Metric(commercial);
        }

        private class Metric implements WarpMetric {
            final int episode;
            final int map;

            Metric(final boolean commercial) {
                if (commercial) {
                    episode = 1;
                    map = WarpFormat.this.warpInt;
                } else {
                    final int evalInt = WarpFormat.this.warpInt > 99
                        ? WarpFormat.this.warpInt % 100
                        : WarpFormat.this.warpInt;
                    
                    episode = evalInt / 10;
                    map = evalInt % 10;
                }
            }

            @Override
            public int getEpisode() {
                return episode;
            }

            @Override
            public int getMap() {
                return map;
            }
        }
    }
    
    public static class MapFormat {
        final String mapString;
        
        public MapFormat(final String mapString) {
            this.mapString = mapString.toLowerCase();
        }
        
        protected int parseAsMapXX() {
            if (mapString.length() != 5 || mapString.lastIndexOf("map") != 0) {
                return -1; // Meh.
            }
            
            final int map;
            try {
                map = Integer.parseInt(mapString.substring(3));
            } catch (NumberFormatException e){
                return -1; // eww
            }

            return map;
        }
        
        protected int parseAsExMx() {
            if (mapString.length() != 4 || mapString.charAt(0) != 'e' || mapString.charAt(2) != 'm') {
                return -1; // Nah.
            }
            
            final char episode = mapString.charAt(1);
            final char mission = mapString.charAt(3);
            
            if (episode < '0' || episode > '9' || mission < '0' || mission > '9')
                return -1;

            return (episode - '0') * 10 + (mission - '0');
        }
        
        public WarpMetric getMetric(final boolean commercial) {
            final int parse = commercial
                ? parseAsMapXX()
                : parseAsExMx();
            
            return new WarpFormat(Math.max(parse, 0)).getMetric(commercial);
        }
    }
}
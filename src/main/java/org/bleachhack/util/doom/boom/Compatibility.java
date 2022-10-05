package org.bleachhack.util.doom.boom;

/** cph - move compatibility levels here so we can use them in d_server.c
 * 
 * @author cph
 *
 */
 
public final class Compatibility {

      /** Doom v1.2 */
      public static final int doom_12_compatibility=0;
      
      public static final int doom_1666_compatibility=1; /* Doom v1.666 */
      public static final int doom2_19_compatibility=2;  /* Doom & Doom 2 v1.9 */
      public static final int ultdoom_compatibility=3;   /* Ultimate Doom and Doom95 */
      public static final int finaldoom_compatibility=4;     /* Final Doom */
      public static final int dosdoom_compatibility=5;     /* DosDoom 0.47 */
      public static final int tasdoom_compatibility=6;     /* TASDoom */
      public static final int boom_compatibility_compatibility=7;      /* Boom's compatibility mode */
      public static final int boom_201_compatibility=8;                /* Boom v2.01 */
      public static final int boom_202_compatibility=9;                /* Boom v2.02 */
      public static final int lxdoom_1_compatibility=10;                /* LxDoom v1.3.2+ */
      public static final int mbf_compatibility=11;                     /* MBF */
      public static final int prboom_1_compatibility=12;                /* PrBoom 2.03beta? */
      public static final int prboom_2_compatibility=13;                /* PrBoom 2.1.0-2.1.1 */
      public static final int prboom_3_compatibility=14;                /* PrBoom 2.2.x */
      public static final int prboom_4_compatibility=15;                /* PrBoom 2.3.x */
      public static final int prboom_5_compatibility=16;              /* PrBoom 2.4.0 */
      public static final int prboom_6_compatibility=17;             /* Latest PrBoom */
      public static final int MAX_COMPATIBILITY_LEVEL=18;           /* Must be last entry */
      /* Aliases follow */
      public static final int boom_compatibility = boom_201_compatibility; /* Alias used by G_Compatibility */
      public static final int best_compatibility = prboom_6_compatibility;
      
      public static final prboom_comp_t[] prboom_comp = {
              new prboom_comp_t(0xffffffff, 0x02020615, false, "-force_monster_avoid_hazards"),
              new prboom_comp_t(0x00000000, 0x02040601, false, "-force_remove_slime_trails"),
              new prboom_comp_t(0x02020200, 0x02040801, false, "-force_no_dropoff"),
              new prboom_comp_t(0x00000000, 0x02040801, false, "-force_truncated_sector_specials"),
              new prboom_comp_t(0x00000000, 0x02040802, false, "-force_boom_brainawake"),
              new prboom_comp_t(0x00000000, 0x02040802, false, "-force_prboom_friction"),
              new prboom_comp_t(0x02020500, 0x02040000, false, "-reject_pad_with_ff"),
              new prboom_comp_t(0xffffffff, 0x02040802, false, "-force_lxdoom_demo_compatibility"),
              new prboom_comp_t(0x00000000, 0x0202061b, false, "-allow_ssg_direct"),
              new prboom_comp_t(0x00000000, 0x02040601, false, "-treat_no_clipping_things_as_not_blocking"),
              new prboom_comp_t(0x00000000, 0x02040803, false, "-force_incorrect_processing_of_respawn_frame_entry"),
              new prboom_comp_t(0x00000000, 0x02040601, false, "-force_correct_code_for_3_keys_doors_in_mbf"),
              new prboom_comp_t(0x00000000, 0x02040601, false, "-uninitialize_crush_field_for_stairs"),
              new prboom_comp_t(0x00000000, 0x02040802, false, "-force_boom_findnexthighestfloor"),
              new prboom_comp_t(0x00000000, 0x02040802, false, "-allow_sky_transfer_in_boom"),
              new prboom_comp_t(0x00000000, 0x02040803, false, "-apply_green_armor_class_to_armor_bonuses"),
              new prboom_comp_t(0x00000000, 0x02040803, false, "-apply_blue_armor_class_to_megasphere"),
              new prboom_comp_t(0x02050001, 0x02050003, false, "-wrong_fixeddiv"),
              new prboom_comp_t(0x02020200, 0x02050003, false, "-force_incorrect_bobbing_in_boom"),
              new prboom_comp_t(0xffffffff, 0x00000000, false, "-boom_deh_parser"),
              new prboom_comp_t(0x00000000, 0x02050007, false, "-mbf_remove_thinker_in_killmobj"),
              new prboom_comp_t(0x00000000, 0x02050007, false, "-do_not_inherit_friendlyness_flag_on_spawn"),
              new prboom_comp_t(0x00000000, 0x02050007, false, "-do_not_use_misc12_frame_parameters_in_a_mushroom")
            };
      
      public static final int PC_MONSTER_AVOID_HAZARDS=0;
      public static final int PC_REMOVE_SLIME_TRAILS=1;
      public static final int PC_NO_DROPOFF=2;
      public static final int PC_TRUNCATED_SECTOR_SPECIALS=3;
      public static final int PC_BOOM_BRAINAWAKE=4;
      public static final int PC_PRBOOM_FRICTION=5;
      public static final int PC_REJECT_PAD_WITH_FF=6;
      public static final int PC_FORCE_LXDOOM_DEMO_COMPATIBILITY=7;
      public static final int PC_ALLOW_SSG_DIRECT=8;
      public static final int PC_TREAT_NO_CLIPPING_THINGS_AS_NOT_BLOCKING=9;
      public static final int PC_FORCE_INCORRECT_PROCESSING_OF_RESPAWN_FRAME_ENTRY=10;
      public static final int PC_FORCE_CORRECT_CODE_FOR_3_KEYS_DOORS_IN_MBF=11;
      public static final int PC_UNINITIALIZE_CRUSH_FIELD_FOR_STAIRS=12;
      public static final int PC_FORCE_BOOM_FINDNEXTHIGHESTFLOOR=13;
      public static final int PC_ALLOW_SKY_TRANSFER_IN_BOOM=14;
      public static final int PC_APPLY_GREEN_ARMOR_CLASS_TO_ARMOR_BONUSES=15;
      public static final int PC_APPLY_BLUE_ARMOR_CLASS_TO_MEGASPHERE=16;
      public static final int PC_WRONG_FIXEDDIV=17;
      public static final int PC_FORCE_INCORRECT_BOBBING_IN_BOOM=18;
      public static final int PC_BOOM_DEH_PARSER=19;
      public static final int PC_MBF_REMOVE_THINKER_IN_KILLMOBJ=20;
      public static final int PC_DO_NOT_INHERIT_FRIENDLYNESS_FLAG_ON_SPAWN=21;
      public static final int PC_DO_NOT_USE_MISC12_FRAME_PARAMETERS_IN_A_MUSHROOM=21;
      public static final int PC_MAX=23;
      
      public enum PC
      {
        PC_MONSTER_AVOID_HAZARDS,
        PC_REMOVE_SLIME_TRAILS,
        PC_NO_DROPOFF,
        PC_TRUNCATED_SECTOR_SPECIALS,
        PC_BOOM_BRAINAWAKE,
        PC_PRBOOM_FRICTION,
        PC_REJECT_PAD_WITH_FF,
        PC_FORCE_LXDOOM_DEMO_COMPATIBILITY,
        PC_ALLOW_SSG_DIRECT,
        PC_TREAT_NO_CLIPPING_THINGS_AS_NOT_BLOCKING,
        PC_FORCE_INCORRECT_PROCESSING_OF_RESPAWN_FRAME_ENTRY,
        PC_FORCE_CORRECT_CODE_FOR_3_KEYS_DOORS_IN_MBF,
        PC_UNINITIALIZE_CRUSH_FIELD_FOR_STAIRS,
        PC_FORCE_BOOM_FINDNEXTHIGHESTFLOOR,
        PC_ALLOW_SKY_TRANSFER_IN_BOOM,
        PC_APPLY_GREEN_ARMOR_CLASS_TO_ARMOR_BONUSES,
        PC_APPLY_BLUE_ARMOR_CLASS_TO_MEGASPHERE,
        PC_WRONG_FIXEDDIV,
        PC_FORCE_INCORRECT_BOBBING_IN_BOOM,
        PC_BOOM_DEH_PARSER,
        PC_MBF_REMOVE_THINKER_IN_KILLMOBJ,
        PC_DO_NOT_INHERIT_FRIENDLYNESS_FLAG_ON_SPAWN,
        PC_DO_NOT_USE_MISC12_FRAME_PARAMETERS_IN_A_MUSHROOM,
        PC_MAX
      };
      
    }

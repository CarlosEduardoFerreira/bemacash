package com.kaching123.pos;

import java.util.HashMap;

/**
 * Created by teli.yin on 10/20/2014.
 */
public class CP850Converter {

    public static final HashMap<String, Byte> string2CP850Byte = new HashMap<String, Byte>();

    static byte c0x20 = (byte) 0x20;
    static byte c0x21 = (byte) 0x21;
    static byte c0x22 = (byte) 0x22;
    static byte c0x23 = (byte) 0x23;
    static byte c0x24 = (byte) 0x24;
    static byte c0x25 = (byte) 0x25;
    static byte c0x26 = (byte) 0x26;
    static byte c0x27 = (byte) 0x27;
    static byte c0x28 = (byte) 0x28;
    static byte c0x29 = (byte) 0x29;
    static byte c0x2a = (byte) 0x2a;
    static byte c0x2b = (byte) 0x2b;
    static byte c0x2c = (byte) 0x2c;
    static byte c0x2d = (byte) 0x2d;
    static byte c0x2e = (byte) 0x2e;
    static byte c0x2f = (byte) 0x2f;

    static byte c0x30 = (byte) 0x30;
    static byte c0x31 = (byte) 0x31;
    static byte c0x32 = (byte) 0x32;
    static byte c0x33 = (byte) 0x33;
    static byte c0x34 = (byte) 0x34;
    static byte c0x35 = (byte) 0x35;
    static byte c0x36 = (byte) 0x36;
    static byte c0x37 = (byte) 0x37;
    static byte c0x38 = (byte) 0x38;
    static byte c0x39 = (byte) 0x39;
    static byte c0x3a = (byte) 0x3a;
    static byte c0x3b = (byte) 0x3b;
    static byte c0x3c = (byte) 0x3c;
    static byte c0x3d = (byte) 0x3d;
    static byte c0x3e = (byte) 0x3e;
    static byte c0x3f = (byte) 0x3f;

    static byte c0x40 = (byte) 0x40;
    static byte c0x41 = (byte) 0x41;
    static byte c0x42 = (byte) 0x42;
    static byte c0x43 = (byte) 0x43;
    static byte c0x44 = (byte) 0x44;
    static byte c0x45 = (byte) 0x45;
    static byte c0x46 = (byte) 0x46;
    static byte c0x47 = (byte) 0x47;
    static byte c0x48 = (byte) 0x48;
    static byte c0x49 = (byte) 0x49;
    static byte c0x4a = (byte) 0x4a;
    static byte c0x4b = (byte) 0x4b;
    static byte c0x4c = (byte) 0x4c;
    static byte c0x4d = (byte) 0x4d;
    static byte c0x4e = (byte) 0x4e;
    static byte c0x4f = (byte) 0x4f;

    static byte c0x50 = (byte) 0x50;
    static byte c0x51 = (byte) 0x51;
    static byte c0x52 = (byte) 0x52;
    static byte c0x53 = (byte) 0x53;
    static byte c0x54 = (byte) 0x54;
    static byte c0x55 = (byte) 0x55;
    static byte c0x56 = (byte) 0x56;
    static byte c0x57 = (byte) 0x57;
    static byte c0x58 = (byte) 0x58;
    static byte c0x59 = (byte) 0x59;
    static byte c0x5a = (byte) 0x5a;
    static byte c0x5b = (byte) 0x5b;
    static byte c0x5c = (byte) 0x5c;
    static byte c0x5d = (byte) 0x5d;
    static byte c0x5e = (byte) 0x5e;
    static byte c0x5f = (byte) 0x5f;

    static byte c0x60 = (byte) 0x60;
    static byte c0x61 = (byte) 0x61;
    static byte c0x62 = (byte) 0x62;
    static byte c0x63 = (byte) 0x63;
    static byte c0x64 = (byte) 0x64;
    static byte c0x65 = (byte) 0x65;
    static byte c0x66 = (byte) 0x66;
    static byte c0x67 = (byte) 0x67;
    static byte c0x68 = (byte) 0x68;
    static byte c0x69 = (byte) 0x69;
    static byte c0x6a = (byte) 0x6a;
    static byte c0x6b = (byte) 0x6b;
    static byte c0x6c = (byte) 0x6c;
    static byte c0x6d = (byte) 0x6d;
    static byte c0x6e = (byte) 0x6e;
    static byte c0x6f = (byte) 0x6f;

    static byte c0x70 = (byte) 0x70;
    static byte c0x71 = (byte) 0x71;
    static byte c0x72 = (byte) 0x72;
    static byte c0x73 = (byte) 0x73;
    static byte c0x74 = (byte) 0x74;
    static byte c0x75 = (byte) 0x75;
    static byte c0x76 = (byte) 0x76;
    static byte c0x77 = (byte) 0x77;
    static byte c0x78 = (byte) 0x78;
    static byte c0x79 = (byte) 0x79;
    static byte c0x7a = (byte) 0x7a;
    static byte c0x7b = (byte) 0x7b;
    static byte c0x7c = (byte) 0x7c;
    static byte c0x7d = (byte) 0x7d;
    static byte c0x7e = (byte) 0x7e;
    static byte c0x7f = (byte) 0x7f;

    static byte c0x80 = (byte) 0x80;
    static byte c0x81 = (byte) 0x81;
    static byte c0x82 = (byte) 0x82;
    static byte c0x83 = (byte) 0x83;
    static byte c0x84 = (byte) 0x84;
    static byte c0x85 = (byte) 0x85;
    static byte c0x86 = (byte) 0x86;
    static byte c0x87 = (byte) 0x87;
    static byte c0x88 = (byte) 0x88;
    static byte c0x89 = (byte) 0x89;
    static byte c0x8a = (byte) 0x8a;
    static byte c0x8b = (byte) 0x8b;
    static byte c0x8c = (byte) 0x8c;
    static byte c0x8d = (byte) 0x8d;
    static byte c0x8e = (byte) 0x8e;
    static byte c0x8f = (byte) 0x8f;


    static byte c0x90 = (byte) 0x90;
    static byte c0x91 = (byte) 0x91;
    static byte c0x92 = (byte) 0x92;
    static byte c0x93 = (byte) 0x93;
    static byte c0x94 = (byte) 0x94;
    static byte c0x95 = (byte) 0x95;
    static byte c0x96 = (byte) 0x96;
    static byte c0x97 = (byte) 0x97;
    static byte c0x98 = (byte) 0x98;
    static byte c0x99 = (byte) 0x99;
    static byte c0x9a = (byte) 0x9a;
    static byte c0x9b = (byte) 0x9b;
    static byte c0x9c = (byte) 0x9c;
    static byte c0x9d = (byte) 0x9d;
    static byte c0x9e = (byte) 0x9e;
    static byte c0x9f = (byte) 0x9f;

    static byte c0xa0 = (byte) 0xa0;
    static byte c0xa1 = (byte) 0xa1;
    static byte c0xa2 = (byte) 0xa2;
    static byte c0xa3 = (byte) 0xa3;
    static byte c0xa4 = (byte) 0xa4;
    static byte c0xa5 = (byte) 0xa5;
    static byte c0xa6 = (byte) 0xa6;
    static byte c0xa7 = (byte) 0xa7;
    static byte c0xa8 = (byte) 0xa8;
    static byte c0xa9 = (byte) 0xa9;
    static byte c0xaa = (byte) 0xaa;
    static byte c0xab = (byte) 0xab;
    static byte c0xac = (byte) 0xac;
    static byte c0xad = (byte) 0xad;
    static byte c0xae = (byte) 0xae;
    static byte c0xaf = (byte) 0xaf;

    static byte c0xb0 = (byte) 0xb0;
    static byte c0xb1 = (byte) 0xb1;
    static byte c0xb2 = (byte) 0xb2;
    static byte c0xb3 = (byte) 0xb3;
    static byte c0xb4 = (byte) 0xb4;
    static byte c0xb5 = (byte) 0xb5;
    static byte c0xb6 = (byte) 0xb6;
    static byte c0xb7 = (byte) 0xb7;
    static byte c0xb8 = (byte) 0xb8;
    static byte c0xb9 = (byte) 0xb9;
    static byte c0xba = (byte) 0xba;
    static byte c0xbb = (byte) 0xbb;
    static byte c0xbc = (byte) 0xbc;
    static byte c0xbd = (byte) 0xbd;
    static byte c0xbe = (byte) 0xbe;
    static byte c0xbf = (byte) 0xbf;

    static byte c0xc0 = (byte) 0xc0;
    static byte c0xc1 = (byte) 0xc1;
    static byte c0xc2 = (byte) 0xc2;
    static byte c0xc3 = (byte) 0xc3;
    static byte c0xc4 = (byte) 0xc4;
    static byte c0xc5 = (byte) 0xc5;
    static byte c0xc6 = (byte) 0xc6;
    static byte c0xc7 = (byte) 0xc7;
    static byte c0xc8 = (byte) 0xc8;
    static byte c0xc9 = (byte) 0xc9;
    static byte c0xca = (byte) 0xca;
    static byte c0xcb = (byte) 0xcb;
    static byte c0xcc = (byte) 0xcc;
    static byte c0xcd = (byte) 0xcd;
    static byte c0xce = (byte) 0xce;
    static byte c0xcf = (byte) 0xcf;

    static byte c0xd0 = (byte) 0xd0;
    static byte c0xd1 = (byte) 0xd1;
    static byte c0xd2 = (byte) 0xd2;
    static byte c0xd3 = (byte) 0xd3;
    static byte c0xd4 = (byte) 0xd4;
    static byte c0xd5 = (byte) 0xd5;
    static byte c0xd6 = (byte) 0xd6;
    static byte c0xd7 = (byte) 0xd7;
    static byte c0xd8 = (byte) 0xd8;
    static byte c0xd9 = (byte) 0xd9;
    static byte c0xda = (byte) 0xda;
    static byte c0xdb = (byte) 0xdb;
    static byte c0xdc = (byte) 0xdc;
    static byte c0xdd = (byte) 0xdd;
    static byte c0xde = (byte) 0xde;
    static byte c0xdf = (byte) 0xdf;

    static byte c0xe0 = (byte) 0xe0;
    static byte c0xe1 = (byte) 0xe1;
    static byte c0xe2 = (byte) 0xe2;
    static byte c0xe3 = (byte) 0xe3;
    static byte c0xe4 = (byte) 0xe4;
    static byte c0xe5 = (byte) 0xe5;
    static byte c0xe6 = (byte) 0xe6;
    static byte c0xe7 = (byte) 0xe7;
    static byte c0xe8 = (byte) 0xe8;
    static byte c0xe9 = (byte) 0xe9;
    static byte c0xea = (byte) 0xea;
    static byte c0xeb = (byte) 0xeb;
    static byte c0xec = (byte) 0xec;
    static byte c0xed = (byte) 0xed;
    static byte c0xee = (byte) 0xee;
    static byte c0xef = (byte) 0xef;

    static byte c0xf0 = (byte) 0xf0;
    static byte c0xf1 = (byte) 0xf1;
    static byte c0xf2 = (byte) 0xf2;
    static byte c0xf3 = (byte) 0xf3;
    static byte c0xf4 = (byte) 0xf4;
    static byte c0xf5 = (byte) 0xf5;
    static byte c0xf6 = (byte) 0xf6;
    static byte c0xf7 = (byte) 0xf7;
    static byte c0xf8 = (byte) 0xf8;
    static byte c0xf9 = (byte) 0xf9;
    static byte c0xfa = (byte) 0xfa;
    static byte c0xfb = (byte) 0xfb;
    static byte c0xfc = (byte) 0xfc;
    static byte c0xfd = (byte) 0xfd;
    static byte c0xfe = (byte) 0xfe;
    static byte c0xff = (byte) 0xff;

    static {
        string2CP850Byte.put("\u0020", c0x20);
        string2CP850Byte.put("\u0021", c0x21);
        string2CP850Byte.put("\"", c0x22);
        string2CP850Byte.put("\u0023", c0x23);
        string2CP850Byte.put("\u0024", c0x24);
        string2CP850Byte.put("\u0025", c0x25);
        string2CP850Byte.put("\u0026", c0x26);
        string2CP850Byte.put("\u0027", c0x27);
        string2CP850Byte.put("\u0028", c0x28);
        string2CP850Byte.put("\u0029", c0x29);
        string2CP850Byte.put("\u002A", c0x2a);
        string2CP850Byte.put("\u002B", c0x2b);
        string2CP850Byte.put("\u002C", c0x2c);
        string2CP850Byte.put("\u002D", c0x2d);
        string2CP850Byte.put("\u002E", c0x2e);
        string2CP850Byte.put("\u002F", c0x2f);

        string2CP850Byte.put("\u0030", c0x30);
        string2CP850Byte.put("\u0031", c0x31);
        string2CP850Byte.put("\u0032", c0x32);
        string2CP850Byte.put("\u0033", c0x33);
        string2CP850Byte.put("\u0034", c0x34);
        string2CP850Byte.put("\u0035", c0x35);
        string2CP850Byte.put("\u0036", c0x36);
        string2CP850Byte.put("\u0037", c0x37);
        string2CP850Byte.put("\u0038", c0x38);
        string2CP850Byte.put("\u0039", c0x39);
        string2CP850Byte.put("\u003A", c0x3a);
        string2CP850Byte.put("\u003B", c0x3b);
        string2CP850Byte.put("\u003C", c0x3c);
        string2CP850Byte.put("\u003D", c0x3d);
        string2CP850Byte.put("\u003E", c0x3e);
        string2CP850Byte.put("\u003F", c0x3f);

        string2CP850Byte.put("\u0040", c0x40);
        string2CP850Byte.put("\u0041", c0x41);
        string2CP850Byte.put("\u0042", c0x42);
        string2CP850Byte.put("\u0043", c0x43);
        string2CP850Byte.put("\u0044", c0x44);
        string2CP850Byte.put("\u0045", c0x45);
        string2CP850Byte.put("\u0046", c0x46);
        string2CP850Byte.put("\u0047", c0x47);
        string2CP850Byte.put("\u0048", c0x48);
        string2CP850Byte.put("\u0049", c0x49);
        string2CP850Byte.put("\u004A", c0x4a);
        string2CP850Byte.put("\u004B", c0x4b);
        string2CP850Byte.put("\u004C", c0x4c);
        string2CP850Byte.put("\u004D", c0x4d);
        string2CP850Byte.put("\u004E", c0x4e);
        string2CP850Byte.put("\u004F", c0x4f);

        string2CP850Byte.put("\u0050", c0x50);
        string2CP850Byte.put("\u0051", c0x51);
        string2CP850Byte.put("\u0052", c0x52);
        string2CP850Byte.put("\u0053", c0x53);
        string2CP850Byte.put("\u0054", c0x54);
        string2CP850Byte.put("\u0055", c0x55);
        string2CP850Byte.put("\u0056", c0x56);
        string2CP850Byte.put("\u0057", c0x57);
        string2CP850Byte.put("\u0058", c0x58);
        string2CP850Byte.put("\u0059", c0x59);
        string2CP850Byte.put("\u005A", c0x5a);
        string2CP850Byte.put("\u005B", c0x5b);
        string2CP850Byte.put("\\", c0x5c);
        string2CP850Byte.put("\u005D", c0x5d);
        string2CP850Byte.put("\u005E", c0x5e);
        string2CP850Byte.put("\u005F", c0x5f);

        string2CP850Byte.put("\u0060", c0x60);
        string2CP850Byte.put("\u0061", c0x61);
        string2CP850Byte.put("\u0062", c0x62);
        string2CP850Byte.put("\u0063", c0x63);
        string2CP850Byte.put("\u0064", c0x64);
        string2CP850Byte.put("\u0065", c0x65);
        string2CP850Byte.put("\u0066", c0x66);
        string2CP850Byte.put("\u0067", c0x67);
        string2CP850Byte.put("\u0068", c0x68);
        string2CP850Byte.put("\u0069", c0x69);
        string2CP850Byte.put("\u006A", c0x6a);
        string2CP850Byte.put("\u006B", c0x6b);
        string2CP850Byte.put("\u006C", c0x6c);
        string2CP850Byte.put("\u006D", c0x6d);
        string2CP850Byte.put("\u006E", c0x6e);
        string2CP850Byte.put("\u006F", c0x6f);

        string2CP850Byte.put("\u0070", c0x70);
        string2CP850Byte.put("\u0071", c0x71);
        string2CP850Byte.put("\u0072", c0x72);
        string2CP850Byte.put("\u0073", c0x73);
        string2CP850Byte.put("\u0074", c0x74);
        string2CP850Byte.put("\u0075", c0x75);
        string2CP850Byte.put("\u0076", c0x76);
        string2CP850Byte.put("\u0077", c0x77);
        string2CP850Byte.put("\u0078", c0x78);
        string2CP850Byte.put("\u0079", c0x79);
        string2CP850Byte.put("\u007A", c0x7a);
        string2CP850Byte.put("\u007B", c0x7b);
        string2CP850Byte.put("\u007C", c0x7c);
        string2CP850Byte.put("\u007D", c0x7d);
        string2CP850Byte.put("\u007E", c0x7e);
        string2CP850Byte.put("\u007F", c0x7f);

        string2CP850Byte.put("\u00c7", c0x80);
        string2CP850Byte.put("\u00fc", c0x81);
        string2CP850Byte.put("\u00e9", c0x82);
        string2CP850Byte.put("\u00e2", c0x83);
        string2CP850Byte.put("\u00e4", c0x84);
        string2CP850Byte.put("\u00e0", c0x85);
        string2CP850Byte.put("\u00e5", c0x86);
        string2CP850Byte.put("\u00e7", c0x87);
        string2CP850Byte.put("\u00eA", c0x88);
        string2CP850Byte.put("\u00eb", c0x89);
        string2CP850Byte.put("\u00e8", c0x8a);
        string2CP850Byte.put("\u00ef", c0x8b);
        string2CP850Byte.put("\u00ee", c0x8c);
        string2CP850Byte.put("\u00ec", c0x8d);
        string2CP850Byte.put("\u00c4", c0x8e);
        string2CP850Byte.put("\u00c5", c0x8f);

        string2CP850Byte.put("\u00c9", c0x90);
        string2CP850Byte.put("\u00E6", c0x91);
        string2CP850Byte.put("\u00C6", c0x92);
        string2CP850Byte.put("\u00f4", c0x93);
        string2CP850Byte.put("\u00f6", c0x94);
        string2CP850Byte.put("\u00f2", c0x95);
        string2CP850Byte.put("\u00fB", c0x96);
        string2CP850Byte.put("\u00f9", c0x97);
        string2CP850Byte.put("\u00ff", c0x98);
        string2CP850Byte.put("\u00d6", c0x99);
        string2CP850Byte.put("\u00dc", c0x9a);
        string2CP850Byte.put("\u00f8", c0x9b);
        string2CP850Byte.put("\u00a3", c0x9c);
        string2CP850Byte.put("\u00d8", c0x9d);
        string2CP850Byte.put("\u00d7", c0x9e);
        string2CP850Byte.put("\u0192", c0x9f);

        string2CP850Byte.put("\u00E1", c0xa0);
        string2CP850Byte.put("\u00ED", c0xa1);
        string2CP850Byte.put("\u00F3", c0xa2);
        string2CP850Byte.put("\u00FA", c0xa3);
        string2CP850Byte.put("\u00F1", c0xa4);
        string2CP850Byte.put("\u00D1", c0xa5);
        string2CP850Byte.put("\u00AA", c0xa6);
        string2CP850Byte.put("\u00BA", c0xa7);
        string2CP850Byte.put("\u00BF", c0xa8);
        string2CP850Byte.put("\u00AE", c0xa9);
        string2CP850Byte.put("\u00AC", c0xaa);
        string2CP850Byte.put("\u00BD", c0xab);
        string2CP850Byte.put("\u00BC", c0xac);
        string2CP850Byte.put("\u00A1", c0xad);
        string2CP850Byte.put("\u00AB", c0xae);
        string2CP850Byte.put("\u00BB", c0xaf);

        string2CP850Byte.put("\u2591", c0xb0);
        string2CP850Byte.put("\u2592", c0xb1);
        string2CP850Byte.put("\u2593", c0xb2);
        string2CP850Byte.put("\u2502", c0xb3);
        string2CP850Byte.put("\u2524", c0xb4);
        string2CP850Byte.put("\u00c1", c0xb5);
        string2CP850Byte.put("\u00c2", c0xb6);
        string2CP850Byte.put("\u00c0", c0xb7);
        string2CP850Byte.put("\u00a9", c0xb8);
        string2CP850Byte.put("\u2563", c0xb9);
        string2CP850Byte.put("\u2551", c0xba);
        string2CP850Byte.put("\u2557", c0xbb);
        string2CP850Byte.put("\u255D", c0xbc);
        string2CP850Byte.put("\u00a2", c0xbd);
        string2CP850Byte.put("\u00a5", c0xbe);
        string2CP850Byte.put("\u2510", c0xbf);

        string2CP850Byte.put("\u2514", c0xc0);
        string2CP850Byte.put("\u2534", c0xc1);
        string2CP850Byte.put("\u252c", c0xc2);
        string2CP850Byte.put("\u251c", c0xc3);
        string2CP850Byte.put("\u2500", c0xc4);
        string2CP850Byte.put("\u253c", c0xc5);
        string2CP850Byte.put("\u00e3", c0xc6);
        string2CP850Byte.put("\u00c3", c0xc7);
        string2CP850Byte.put("\u255a", c0xc8);
        string2CP850Byte.put("\u2554", c0xc9);
        string2CP850Byte.put("\u2569", c0xca);
        string2CP850Byte.put("\u2566", c0xcb);
        string2CP850Byte.put("\u2560", c0xcc);
        string2CP850Byte.put("\u2550", c0xcd);
        string2CP850Byte.put("\u256c", c0xce);
        string2CP850Byte.put("\u00a4", c0xcf);

        string2CP850Byte.put("\u00f0", c0xd0);
        string2CP850Byte.put("\u00d0", c0xd1);
        string2CP850Byte.put("\u00ca", c0xd2);
        string2CP850Byte.put("\u00cb", c0xd3);
        string2CP850Byte.put("\u00c8", c0xd4);
        string2CP850Byte.put("\u0131", c0xd5);
        string2CP850Byte.put("\u00cd", c0xd6);
        string2CP850Byte.put("\u00ce", c0xd7);
        string2CP850Byte.put("\u00cf", c0xd8);
        string2CP850Byte.put("\u2518", c0xd9);
        string2CP850Byte.put("\u250c", c0xda);
        string2CP850Byte.put("\u2588", c0xdb);
        string2CP850Byte.put("\u2584", c0xdc);
        string2CP850Byte.put("\u00a6", c0xdd);
        string2CP850Byte.put("\u00cc", c0xde);
        string2CP850Byte.put("\u2580", c0xdf);

        string2CP850Byte.put("\u00d3", c0xe0);
        string2CP850Byte.put("\u00df", c0xe1);
        string2CP850Byte.put("\u00d4", c0xe2);
        string2CP850Byte.put("\u00d2", c0xe3);
        string2CP850Byte.put("\u00f5", c0xe4);
        string2CP850Byte.put("\u00d5", c0xe5);
        string2CP850Byte.put("\u00b5", c0xe6);
        string2CP850Byte.put("\u00fe", c0xe7);
        string2CP850Byte.put("\u00de", c0xe8);
        string2CP850Byte.put("\u00da", c0xe9);
        string2CP850Byte.put("\u00db", c0xea);
        string2CP850Byte.put("\u00d9", c0xeb);
        string2CP850Byte.put("\u00fd", c0xec);
        string2CP850Byte.put("\u00dd", c0xed);
        string2CP850Byte.put("\u00af", c0xee);
        string2CP850Byte.put("\u00b4", c0xef);

        string2CP850Byte.put("\u00ad", c0xf0);
        string2CP850Byte.put("\u00b1", c0xf1);
        string2CP850Byte.put("\u2017", c0xf2);
        string2CP850Byte.put("\u00be", c0xf3);
        string2CP850Byte.put("\u00b6", c0xf4);
        string2CP850Byte.put("\u00a7", c0xf5);
        string2CP850Byte.put("\u00f7", c0xf6);
        string2CP850Byte.put("\u00b8", c0xf7);
        string2CP850Byte.put("\u00b0", c0xf8);
        string2CP850Byte.put("\u00a8", c0xf9);
        string2CP850Byte.put("\u00b7", c0xfa);
        string2CP850Byte.put("\u00b9", c0xfb);
        string2CP850Byte.put("\u00b3", c0xfc);
        string2CP850Byte.put("\u00b2", c0xfd);
        string2CP850Byte.put("\u25a0", c0xfe);
        string2CP850Byte.put("\u00a0", c0xff);
    }

}

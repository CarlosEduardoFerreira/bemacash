package com.bematechus.bemaUtils;

/**
 * Created by teli.yin on 10/20/2014.
 */


public class CodePage866Converter extends CodePageConverter{

    public CodePage866Converter () {
        map.putAll(AsciiTable.ascii);

        map.put('\u0410', (byte) 0x80);
        map.put('\u0411', (byte) 0x81);
        map.put('\u0412', (byte) 0x82);
        map.put('\u0413', (byte) 0x83);
        map.put('\u0414', (byte) 0x84);
        map.put('\u0415', (byte) 0x85);
        map.put('\u0416', (byte) 0x86);
        map.put('\u0417', (byte) 0x87);
        map.put('\u0418', (byte) 0x88);
        map.put('\u0419', (byte) 0x89);
        map.put('\u041a', (byte) 0x8a);
        map.put('\u041b', (byte) 0x8b);
        map.put('\u041c', (byte) 0x8c);
        map.put('\u041d', (byte) 0x8d);
        map.put('\u041e', (byte) 0x8e);
        map.put('\u041f', (byte) 0x8f);

        map.put('\u0420', (byte) 0x90);
        map.put('\u0421', (byte) 0x91);
        map.put('\u0422', (byte) 0x92);
        map.put('\u0423', (byte) 0x93);
        map.put('\u0424', (byte) 0x94);
        map.put('\u0425', (byte) 0x95);
        map.put('\u0426', (byte) 0x96);
        map.put('\u0427', (byte) 0x97);
        map.put('\u0428', (byte) 0x98);
        map.put('\u0429', (byte) 0x99);
        map.put('\u042a', (byte) 0x9a);
        map.put('\u042b', (byte) 0x9b);
        map.put('\u042c', (byte) 0x9c);
        map.put('\u042d', (byte) 0x9d);
        map.put('\u042e', (byte) 0x9e);
        map.put('\u042f', (byte) 0x9f);

        map.put('\u0430', (byte) 0xa0);
        map.put('\u0431', (byte) 0xa1);
        map.put('\u0432', (byte) 0xa2);
        map.put('\u0433', (byte) 0xa3);
        map.put('\u0434', (byte) 0xa4);
        map.put('\u0435', (byte) 0xa5);
        map.put('\u0436', (byte) 0xa6);
        map.put('\u0437', (byte) 0xa7);
        map.put('\u0438', (byte) 0xa8);
        map.put('\u0439', (byte) 0xa9);
        map.put('\u043a', (byte) 0xaa);
        map.put('\u043b', (byte) 0xab);
        map.put('\u043c', (byte) 0xac);
        map.put('\u043d', (byte) 0xad);
        map.put('\u043e', (byte) 0xae);
        map.put('\u043f', (byte) 0xaf);

        map.put('\u2591', (byte) 0xb0);
        map.put('\u2592', (byte) 0xb1);
        map.put('\u2593', (byte) 0xb2);
        map.put('\u2502', (byte) 0xb3);
        map.put('\u2524', (byte) 0xb4);
        map.put('\u2561', (byte) 0xb5);
        map.put('\u2562', (byte) 0xb6);
        map.put('\u2556', (byte) 0xb7);
        map.put('\u2555', (byte) 0xb8);
        map.put('\u2563', (byte) 0xb9);
        map.put('\u2551', (byte) 0xba);
        map.put('\u2557', (byte) 0xbb);
        map.put('\u255D', (byte) 0xbc);
        map.put('\u255c', (byte) 0xbd);
        map.put('\u255b', (byte) 0xbe);
        map.put('\u2510', (byte) 0xbf);

        map.put('\u2514', (byte) 0xc0);
        map.put('\u2534', (byte) 0xc1);
        map.put('\u252c', (byte) 0xc2);
        map.put('\u251c', (byte) 0xc3);
        map.put('\u2500', (byte) 0xc4);
        map.put('\u253c', (byte) 0xc5);
        map.put('\u255e', (byte) 0xc6);
        map.put('\u255f', (byte) 0xc7);
        map.put('\u255a', (byte) 0xc8);
        map.put('\u2554', (byte) 0xc9);
        map.put('\u2569', (byte) 0xca);
        map.put('\u2566', (byte) 0xcb);
        map.put('\u2560', (byte) 0xcc);
        map.put('\u2550', (byte) 0xcd);
        map.put('\u256c', (byte) 0xce);
        map.put('\u2567', (byte) 0xcf);

        map.put('\u2568', (byte) 0xd0);
        map.put('\u2564', (byte) 0xd1);
        map.put('\u2565', (byte) 0xd2);
        map.put('\u2559', (byte) 0xd3);
        map.put('\u2558', (byte) 0xd4);
        map.put('\u2552', (byte) 0xd5);
        map.put('\u2553', (byte) 0xd6);
        map.put('\u256b', (byte) 0xd7);
        map.put('\u256a', (byte) 0xd8);
        map.put('\u2518', (byte) 0xd9);
        map.put('\u250c', (byte) 0xda);
        map.put('\u2588', (byte) 0xdb);
        map.put('\u2584', (byte) 0xdc);
        map.put('\u258c', (byte) 0xdd);
        map.put('\u2590', (byte) 0xde);
        map.put('\u2580', (byte) 0xdf);

        map.put('\u0440', (byte) 0xe0);
        map.put('\u0441', (byte) 0xe1);
        map.put('\u0442', (byte) 0xe2);
        map.put('\u0443', (byte) 0xe3);
        map.put('\u0444', (byte) 0xe4);
        map.put('\u0445', (byte) 0xe5);
        map.put('\u0446', (byte) 0xe6);
        map.put('\u0447', (byte) 0xe7);
        map.put('\u0448', (byte) 0xe8);
        map.put('\u0449', (byte) 0xe9);
        map.put('\u044a', (byte) 0xea);
        map.put('\u044b', (byte) 0xeb);
        map.put('\u044c', (byte) 0xec);
        map.put('\u044d', (byte) 0xed);
        map.put('\u044e', (byte) 0xee);
        map.put('\u044f', (byte) 0xef);

        map.put('\u0401', (byte) 0xf0);
        map.put('\u0451', (byte) 0xf1);
        map.put('\u0404', (byte) 0xf2);
        map.put('\u0454', (byte) 0xf3);
        map.put('\u0407', (byte) 0xf4);
        map.put('\u0457', (byte) 0xf5);
        map.put('\u040e', (byte) 0xf6);
        map.put('\u045e', (byte) 0xf7);
        map.put('\u00b0', (byte) 0xf8);
        map.put('\u2219', (byte) 0xf9);
        map.put('\u00b7', (byte) 0xfa);
        map.put('\u221a', (byte) 0xfb);
        map.put('\u2116', (byte) 0xfc);
        map.put('\u00a4', (byte) 0xfd);
        map.put('\u25a0', (byte) 0xfe);
        map.put('\u00a0', (byte) 0xff);
    }


}

package com.codergists;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

/**
 * @author tvajjala
 */
 class CidrBlockBuilderTest {

    @Test
    void subnetBlockWithMask24Test() {

        //given:
        NetworkSubnet networkSubnet = NetworkSubnet.builder(NetworkSubnet.Protocol.IPV4)
                .fromMasterBlock("10.0.128.0/17")//<-- vcnSubnet
                .withSubnetMask(NetworkSubnet.Mask._24)
                .build();

        //when:
        Iterator<String> iterator = networkSubnet.getSubnetCidrBlocks();

        //then: with 256 IP blocks
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.128.0/24", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.129.0/24", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.130.0/24", iterator.next());

    }


    @Test
    void subnetBlockWithMask28Test() {

        //given: scenario
        NetworkSubnet networkSubnet = NetworkSubnet.builder(NetworkSubnet.Protocol.IPV4)
                .fromMasterBlock("10.0.128.0/17")//<-- vcnSubnet
                .withSubnetMask(NetworkSubnet.Mask._28)
                .build();

        //when: invoke build cidr block
        Iterator<String> iterator = networkSubnet.getSubnetCidrBlocks();

        //then: expect 128 IP slots
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.128.0/28", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.128.16/28", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.128.32/28", iterator.next());

    }

    @Test
    void foundOverlappingAddressTest() {
        NetworkSubnet networkSubnet = NetworkSubnet.builder(NetworkSubnet.Protocol.IPV4)
                .fromMasterBlock("10.0.128.0/17")//<-- vcnSubnet
                .withSubnetMask(NetworkSubnet.Mask._28)
                .build();

        //given: overlapping address [10.0.128.30, 10.0.128.31]
        boolean flag = networkSubnet.hasAnyOverlappingAddress("10.0.128.30/27", "10.0.128.0/27");

        Assertions.assertTrue(flag);

    }
}

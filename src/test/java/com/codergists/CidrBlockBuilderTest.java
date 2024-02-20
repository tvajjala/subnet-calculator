package com.codergists;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

/**
 * @author tvajjala
 *
 * https://www.site24x7.com/tools/ipv4-subnetcalculator.html
 */
class CidrBlockBuilderTest {

    @Test
    void subnetBlockWithMask20Test() {

        //given:
        NetworkSubnet networkSubnet = NetworkSubnet.builder(NetworkSubnet.Protocol.IPV4)
                .fromMasterBlock("10.0.128.0/17")//<-- vcnSubnet
                .withSubnetMask(NetworkSubnet.Mask._20)
                .build();

        //assuming max subnets allowed are 8
        Assumptions.assumeTrue(8 == networkSubnet.maxSubnetsAllowed());

        //when:
        Iterator<String> iterator = networkSubnet.getSubnetCidrBlocks();

        //then: with 256 IP blocks
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.128.0/20", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.144.0/20", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.160.0/20", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.176.0/20", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.192.0/20", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.208.0/20", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.224.0/20", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.240.0/20", iterator.next());

        // max subnets reached for given vcn range
        Assumptions.assumeFalse(iterator.hasNext());
    }

    @Test
    void subnetBlockWithMask24Test() {

        //given:
        NetworkSubnet networkSubnet = NetworkSubnet.builder(NetworkSubnet.Protocol.IPV4)
                .fromMasterBlock("10.0.128.0/17")//<-- vcnSubnet
                .withSubnetMask(NetworkSubnet.Mask._24)
                .build();

        //assuming max subnets allowed are 32
        Assumptions.assumeTrue(128 == networkSubnet.maxSubnetsAllowed());

        //when:
        Iterator<String> iterator = networkSubnet.getSubnetCidrBlocks();

        //then: with 256 IP blocks
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.128.0/24", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.129.0/24", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.130.0/24", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.131.0/24", iterator.next());
    }

    @Test
    void subnetBlockWithMask22Test() {

        //given:
        NetworkSubnet networkSubnet = NetworkSubnet.builder(NetworkSubnet.Protocol.IPV4)
                .fromMasterBlock("10.0.128.0/17") //<-- vcnSubnet
                .withSubnetMask(NetworkSubnet.Mask._22)
                .build();

        //assuming max subnets allowed are 32
        Assumptions.assumeTrue(32 == networkSubnet.maxSubnetsAllowed());

        //when:
        Iterator<String> iterator = networkSubnet.getSubnetCidrBlocks();

        //then: with 1024 IP blocks
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.128.0/22", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.132.0/22", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.136.0/22", iterator.next());

        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("10.0.140.0/22", iterator.next());

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
package com.codergists;


import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;

/**
 * https://mxtoolbox.com/subnetcalculator.aspx
 */
@Slf4j
public class CidrBlockCalculator {


    public static void main(String[] args) {

        NetworkSubnet sfcpSubnet = NetworkSubnet.builder(NetworkSubnet.Protocol.IPV4)
                .fromMasterBlock("10.0.128.0/17")//<-- vcnSubnet
                .withSubnetMask(NetworkSubnet.Mask._24)
                .build();

        Iterator<String> iterator = sfcpSubnet.getSubnetCidrBlocks();

        while (iterator.hasNext()) {
            log.info(iterator.next());
        }


        System.out.println(sfcpSubnet.getTotalIPs());
        System.out.println(sfcpSubnet.getTotalUsableIPs());


      /*  cidrBlock.getCIDRBlockWithFullIPList().entrySet().forEach(stringListEntry -> {

            System.out.println("----- " + stringListEntry.getKey() + "  ----- ");
            System.out.print(stringListEntry.getValue());
            System.out.println();
            System.out.println();


        });*/

    }


}

package com.codergists;

import io.vavr.collection.Stream;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * This is simple utility to create Classless Inter-Domain Routing(CIDR) blocks for given masterBlock
 * <p>
 * Algorithm based on https://en.wikipedia.org/wiki/Classless_Inter-Domain_Routing
 *
 * <p>
 * It only generates CIDR block with 256,128,64,32,16 IPs (i.e, subnet mask 24,25,26,27,28)
 * <p>
 * 1. Returns max number of given for given vcnRange
 * 2. Returns all the CIDR blocks
 * 3. Return CIDR blocks along with full IP list
 * </ol>
 *
 * @author tvajjala
 */
@Slf4j
public class NetworkSubnet {

    /**
     * IPv4 uses 32-bit IP address
     */
    private static final int IPV4_MAX = 32;

    /**
     * Subnet mask
     */
    private String masterBlockIPV4;

    /**
     * VCN Mask
     */
    private Integer masterBlockMask;

    /**
     * Mask to split CIDR range
     */
    private Mask subnetMask;


    /**
     * @param masterBlockIPV4 masterBlockIPV4
     * @param masterBlockMask masterBlockMask
     * @param subnetMask      subnetMask
     */
    private NetworkSubnet(String masterBlockIPV4, Integer masterBlockMask, Mask subnetMask) {
        this.masterBlockIPV4 = masterBlockIPV4;
        this.masterBlockMask = masterBlockMask;
        this.subnetMask = subnetMask;
    }

    /**
     * Max subnets allowed
     *
     * @return max subnets allowed
     */
    public int maxSubnetsAllowed() {
        return Double.valueOf(Math.pow(2, (subnetMask.getValue() - masterBlockMask))).intValue();
    }

    /**
     * Max Byte Capacity
     * <p>
     * IPV4 (X.X.X.X) address represents 4 block with each of 1 Byte (with range (0- 255) decimals)
     * <p>
     */
    private static final int MAX = ((int) Math.pow(2, 8) - 1);

    public boolean hasAnyOverlappingAddress(List<String> existingCidrList, String cidrToBeCreated) {
        return existingCidrList.stream().anyMatch(existing -> hasAnyOverlappingAddress(existing, cidrToBeCreated));
    }

    public boolean hasAnyOverlappingAddress(String existingCidr, String cidrToBeCreated) {
        List<String> m1List = getIPList(existingCidr);
        List<String> m2List = getIPList(cidrToBeCreated);
        m2List.retainAll(m1List);// retains common elements in m2
        log.info("Found overlapping {}", m2List);
        return !m2List.isEmpty();
    }

    public Iterator<String> getSubnetCidrBlocks() {

        return new CIDRBlockHolder(masterBlockIPV4, subnetMask, maxSubnetsAllowed());
    }

    class CIDRBlockHolder implements Iterator<String> {

        private int[] blocks;
        /**
         * subnet block ip range required
         */
       // private int maxIpCount;
        Mask subnetMask;
        private int b0;
        private int b1;
        private int b2;
        private int b3;
        private boolean first;
        private int maxSubnetsAllowed;

        CIDRBlockHolder(String startingIp, Mask subnetMask, int maxSubnetsAllowed) {
            this.blocks = stream(startingIp.split("\\.")).mapToInt(Integer::parseInt).toArray();
            b0 = blocks[0];
            b1 = blocks[1];
            b2 = blocks[2];
            b3 = blocks[3];
            first = true;
          //  this.maxIpCount = (int) Math.pow(2, (IPV4_MAX - subnetMask.getValue()));
            this.subnetMask=subnetMask;
            this.maxSubnetsAllowed = maxSubnetsAllowed;
        }

        boolean hasNextInvoked = false;
        int subnetCounter = 0;

        @Override
        public boolean hasNext() {
            /* only  2 ^(subnetMask-vcnMask) subnets allowed */
            if (subnetCounter >= maxSubnetsAllowed) {
                log.warn("Max allowed subnets exceeded");
                return false;
            }
            subnetCounter +=1;
            hasNextInvoked = true;
            /* first time just return true, blocks always exists*/
            if (first) {
                first = false;
                return true;
            }

            increment();
            //return b0 <= MAX;// stop if 255.255.255.x
            return b2 <= MAX; // stop if x.x.255.x
        }

        @Override
        public String next() {
            if (!hasNextInvoked) {
                throw new IllegalStateException("Invalid state - must invoke hasNext before calling next");
            }
            hasNextInvoked = false;
            String subnet = String.format("%d.%d.%d.%d/%d", b0, b1, b2, b3, subnetMask.getValue());
            log.info("Initial IP " + String.format("%d.%d.%d.%d", b0, b1, b2, b3));
            log.info("Total IPs " + ipCount(subnetMask.getValue()));
            log.info("Full IPList " + getFullIPList());
            return subnet;
        }


        private void increment() {
            //TODO: more optimise with bit shifting
            if (subnetMask == Mask._20) {
                b2 += 16;
            }else if (subnetMask == Mask._21) {
                b2 += 8;
            }else if (subnetMask == Mask._22) {
                b2 += 4;
            }else if (subnetMask == Mask._23) {
                b2 += 2;
            }else if(subnetMask == Mask._24){
                b2 += 1;
            }else if (subnetMask.getValue() < 256) {
               int  maxIpCount = (int) Math.pow(2, (IPV4_MAX - subnetMask.getValue()));
                if ((b3 + maxIpCount) >= MAX) { //0+255, next b3 will be 0, and b2++
                    b3 = 0;
                    b2++;
                } else {
                    b3 = (b3 + maxIpCount);
                }
            }else{
                throw new UnsupportedOperationException("Subnet not supported");
            }

         /*   if (b2 >= (MAX - 1)) {
                b2 = MAX;//max 255
                b1++;
            }

            if (b1 >= (MAX - 1)) {
                b2 = MAX;//max 255
                b1 = MAX;//max 255
                b0++;
            }*/
        }

    }


    private List<String> getFullIPList() {
        return getIPList(masterBlockIPV4, masterBlockMask);
    }

    /**
     * Returns list of all IPs
     *
     * @param initialIP  x.x.x.x
     * @param subnetMask y
     * @return list of all IPs
     */
    private List<String> getIPList(String initialIP, int subnetMask) {
        final int[] bytes = stream(initialIP.split("\\.")).mapToInt(Integer::parseInt).toArray();
        int b3 = bytes[3];
        int max = b3 + ipCount(subnetMask);
        max = (max > MAX) ? MAX : (max);   // Utility only support 24,25,26,27,28 masks.
        return Stream.range(b3, max)
                .map(i -> String.format("%d.%d.%d.%d", bytes[0], bytes[1], bytes[2], i))
                .collect(toList());
    }

    /**
     * Returns list of all IPs
     *
     * @param subnetWithMask x.x.x.x/y
     * @return list of IPs
     */
    private List<String> getIPList(String subnetWithMask) {
        int m1Index = subnetWithMask.indexOf("/");
        String initialIP = subnetWithMask.substring(0, m1Index);

        int m1Mask = Integer.parseInt(subnetWithMask.substring(m1Index + 1));
        return getIPList(initialIP, m1Mask);
    }

    /**
     * IP Count for the given mask
     *
     * @param subnetMask subnetMask
     * @return total IP Count
     */
    private static int ipCount(int subnetMask) {
        return (int) Math.pow(2, (IPV4_MAX - subnetMask));
    }

    public int getTotalIPs() {
        return ipCount(masterBlockMask);
    }


    public int getTotalUsableIPs() {
        int totalIPs = getTotalIPs();
        return (totalIPs <= 2) ? totalIPs : (totalIPs - 2);
    }

    public static Builder builder(Protocol protocol) {
        return new Builder(protocol);
    }


    /**
     * Builder
     */
    public static class Builder {

        private String masterBlock;

        private Protocol protocol;

        private Mask subnetMask;


        public Builder(Protocol protocol) {

            this.protocol = protocol;
        }

        /**
         * VCN Subnet Range
         *
         * @param masterBlock masterBlock block
         * @return Builder
         */
        Builder fromMasterBlock(String masterBlock) {
            this.masterBlock = masterBlock;
            return this;
        }

        Builder withSubnetMask(Mask subnetMask) {
            this.subnetMask = subnetMask;
            return this;
        }

        public NetworkSubnet build() {
            int slashIndex = masterBlock.indexOf("/");
            String masterBlockIPv4 = masterBlock.substring(0, slashIndex);
            Integer masterBlockMask = Integer.parseInt(masterBlock.substring(slashIndex + 1));
            if (protocol.isIPv4() && !isValidIPV4(masterBlockIPv4)) {
                throw new IllegalArgumentException("Invalid IPV4 range provided");
            }

            // Must be within (32 > subnetMask > masterBlockMask)
            if ((subnetMask.getValue() > 32) || (masterBlockMask > subnetMask.getValue())) {
                throw new IllegalArgumentException("Invalid subnetMask value provided");
            }

            return new NetworkSubnet(masterBlockIPv4, masterBlockMask, subnetMask);
        }


        private boolean isValidIPV4(String ipv4) {
            if (ipv4.split("\\.").length != 4) {
                return false;
            }
            //validate if the input decimal block having values between 0,255
            return stream(ipv4.split("\\.")).map(Integer::parseInt).noneMatch(bit -> (0 < bit && bit > MAX));
        }

    }

    enum Protocol {

        /**
         * Only Supports IPV4
         */
        IPV4,
        /**
         * IPV6 Supports not yet implemented
         */
        IPV6;

        public boolean isIPv4() {
            return this == IPV4;
        }

        public boolean isIPv6() {
            return this == IPV6;
        }

    }

    enum Mask {
        _20(20, 4096),
        _21(21, 2048),

        _22(22, 1024),
        _23(23, 512),
        _24(24, 256),
        _25(25, 128),
        _26(26, 64),
        _27(27, 32),
        _28(28, 16);

        private int mask;

        private int ipRange;

        Mask(int mask, int ipRange) {
            this.mask = mask;
            this.ipRange = ipRange;
        }

        int getValue() {
            return mask;
        }

        int getIpRange() {
            return ipRange;
        }

    }
}

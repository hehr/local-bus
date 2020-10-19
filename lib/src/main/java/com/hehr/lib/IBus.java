package com.hehr.lib;

public interface IBus {

    /**
     * local socket bind address
     */
    String DEFAULT_ADDRESS = "CN.HEHR.LOCAL.BUS";


    /**
     * bus operate type
     *
     * @author hehr
     */
    enum Type {
        /**
         * mount client
         */
        join(0x001),
        /**
         * subscribe
         */
        subscribe(0x002),
        /**
         * unsubscribe
         */
        unsubscribe(0x003),

        /**
         * broadcast
         */
        broadcast(0x004),
        /**
         * exit
         */
        exit(0x005),

        ;

        int value;

        Type(int value) {
            this.value = value;
        }


        /**
         * find enum type by integer
         *
         * @param value int
         * @return {@link Type}
         */
        static final Type findTypeByInteger(int value) {
            for (Type type : values()) {
                if (value == type.value) {
                    return type;
                }
            }

            return null;
        }

        /**
         * illegal type
         *
         * @param value type
         * @return boolean
         */
        public static boolean isIllegal(int value) {
            return findTypeByInteger(value) == null;
        }

    }


}

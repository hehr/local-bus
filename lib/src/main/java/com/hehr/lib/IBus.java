package com.hehr.lib;

public interface IBus {

    int DEFAULT_PORT = 50001;

    String IP = "127.0.0.1";

    String DELIMITER = "\t" + "&";

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

        public int value;

        Type(int value) {
            this.value = value;
        }


        /**
         * find enum type by integer
         *
         * @param value int
         * @return {@link Type}
         */
        public static final Type findTypeByInteger(int value) {
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

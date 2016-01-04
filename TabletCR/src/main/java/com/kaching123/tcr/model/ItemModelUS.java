package com.kaching123.tcr.model;

/**
 * Created by alboyko on 04.01.2016.
 */
public class ItemModelUS {

    private String guid;

    private ItemModelUS() {
        // private
    }

    public void addGuid(String guid) {
        this.guid = guid;
    }

    public String getGuid() {
        return guid;
    }

    // new setter()
    // new setter()
    // new setter()

    public static class Builder {
        private ItemModelUS instance;

        public Builder() {
            instance = new ItemModelUS();
        }

        public Builder addGuid(String guid) {
            instance.addGuid(guid);
            return this;
        }
        // new setter()
        // new setter()
        // new setter()


        public ItemModelUS build() {
            return instance;
        }
    }


}

/*
*         ItemModelUS itam = new ItemModelUS.Builder()
                .addGuid(String.valueOf(guid))
                .build();*/

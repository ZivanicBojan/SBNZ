package com.ftn.sbnz.model.models;

public class Source {
    private String name;
    private Reputation reputation;

    public Source() {}

    public Source(String name, Reputation reputation) {
        this.name = name;
        this.reputation = reputation;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Reputation getReputation() { return reputation; }
    public void setReputation(Reputation reputation) { this.reputation = reputation; }

    @Override
    public String toString() {
        return "Source{" + "name='" + name + '\'' + ", reputation=" + reputation + '}';
    }
}

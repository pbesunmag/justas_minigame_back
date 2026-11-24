package com.jousting.rpg.domain.model;

import java.time.LocalDateTime;

/**
 * Representa una partida guardada vinculada a una cuenta de usuario específica,
 * incluyendo la personalización estética del caballero y su corcel.
 */
public class SaveGame {

    private Long id;
    private final String ownerUsername;
    private String saveName;
    private Knight protagonist;

    // Personalización del caballero y su caballo
    private int age;
    private String bodyType;
    private String skinTone;
    private String hairStyle;
    private String hairColor;
    private String horseName;
    private String horseColor;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SaveGame(Long id, String ownerUsername, String saveName, Knight protagonist) {
        this(id, ownerUsername, saveName, protagonist, 22, "ATHLETIC", "LIGHT", "SHORT", "BROWN", "Rocín", "BAY");
    }

    public SaveGame(Long id, String ownerUsername, String saveName, Knight protagonist,
                    int age, String bodyType, String skinTone, String hairStyle,
                    String hairColor, String horseName, String horseColor) {
        this.id = id;
        this.ownerUsername = ownerUsername;
        this.saveName = saveName;
        this.protagonist = protagonist;
        this.age = age;
        this.bodyType = bodyType;
        this.skinTone = skinTone;
        this.hairStyle = hairStyle;
        this.hairColor = hairColor;
        this.horseName = horseName;
        this.horseColor = horseColor;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public SaveGame(String ownerUsername, String saveName, Knight protagonist) {
        this(null, ownerUsername, saveName, protagonist);
    }

    public SaveGame(String ownerUsername, String saveName, Knight protagonist,
                    int age, String bodyType, String skinTone, String hairStyle,
                    String hairColor, String horseName, String horseColor) {
        this(null, ownerUsername, saveName, protagonist, age, bodyType, skinTone, hairStyle, hairColor, horseName, horseColor);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public String getSaveName() {
        return saveName;
    }

    public void setSaveName(String saveName) {
        this.saveName = saveName;
        this.updatedAt = LocalDateTime.now();
    }

    public Knight getProtagonist() {
        return protagonist;
    }

    public void setProtagonist(Knight protagonist) {
        this.protagonist = protagonist;
        this.updatedAt = LocalDateTime.now();
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
        this.updatedAt = LocalDateTime.now();
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
        this.updatedAt = LocalDateTime.now();
    }

    public String getSkinTone() {
        return skinTone;
    }

    public void setSkinTone(String skinTone) {
        this.skinTone = skinTone;
        this.updatedAt = LocalDateTime.now();
    }

    public String getHairStyle() {
        return hairStyle;
    }

    public void setHairStyle(String hairStyle) {
        this.hairStyle = hairStyle;
        this.updatedAt = LocalDateTime.now();
    }

    public String getHairColor() {
        return hairColor;
    }

    public void setHairColor(String hairColor) {
        this.hairColor = hairColor;
        this.updatedAt = LocalDateTime.now();
    }

    public String getHorseName() {
        return horseName;
    }

    public void setHorseName(String horseName) {
        this.horseName = horseName;
        this.updatedAt = LocalDateTime.now();
    }

    public String getHorseColor() {
        return horseColor;
    }

    public void setHorseColor(String horseColor) {
        this.horseColor = horseColor;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

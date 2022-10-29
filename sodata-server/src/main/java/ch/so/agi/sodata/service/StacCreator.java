package ch.so.agi.sodata.service;

import ch.so.agi.sodata.dto.ThemePublicationDTO;

public interface StacCreator {
    public void create(String collectionFilePath, ThemePublicationDTO themePublication);
}

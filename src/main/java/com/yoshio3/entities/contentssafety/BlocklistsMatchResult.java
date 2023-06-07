package com.yoshio3.entities.contentssafety;

import java.util.UUID;

public record BlocklistsMatchResult (String blocklistName, UUID blockItemID, String blockItemText, long offset, long length) {}

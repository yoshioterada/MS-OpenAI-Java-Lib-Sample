package com.yoshio3.entities.contentssafety;

import java.util.List;

public record ResposeMessageFromContentSafety (List<BlocklistsMatchResult> blocklistsMatchResults, ContentSafetyResult hateResult, ContentSafetyResult selfHarmResult, ContentSafetyResult sexualResult, ContentSafetyResult violenceResult) {}

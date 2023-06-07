package com.yoshio3.entities.contentssafety;

import java.util.List;

public record CreateContentsSafetyMessage (String text, List<String> categories, List<String> blocklistNames, boolean breakByBlocklists){}

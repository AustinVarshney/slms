package com.java.slms.util;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing class names with sections
 * Examples:
 * - "L.K.G.-A" -> class="L.K.G.", section="A"
 * - "LKG-A" -> class="LKG", section="A"
 * - "11-1A" -> class="11", section="1A"
 * - "1-A" -> class="1", section="A"
 * - "10A" -> class="10", section="A"
 */
@Slf4j
public class ClassNameParser {
    
    // Pattern to match various class-section formats
    private static final Pattern CLASS_SECTION_PATTERN = Pattern.compile("^([A-Z0-9\\.]+)[-\\s]?([A-Z0-9]+)$", Pattern.CASE_INSENSITIVE);
    
    public static class ParsedClassName {
        private final String className;
        private final String section;
        private final String fullName;
        
        public ParsedClassName(String className, String section) {
            this.className = className.toUpperCase();
            this.section = section.toUpperCase();
            this.fullName = this.className + "-" + this.section;
        }
        
        public String getClassName() {
            return className;
        }
        
        public String getSection() {
            return section;
        }
        
        public String getFullName() {
            return fullName;
        }
        
        @Override
        public String toString() {
            return fullName;
        }
    }
    
    /**
     * Parse a class name input into class and section components
     * @param input The input string (e.g., "L.K.G.-A", "LKG-A", "11-1A")
     * @return ParsedClassName object with separated class and section
     */
    public static ParsedClassName parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Class name cannot be empty");
        }
        
        String trimmed = input.trim();
        
        // Try to match the pattern
        Matcher matcher = CLASS_SECTION_PATTERN.matcher(trimmed);
        
        if (matcher.matches()) {
            String classpart = matcher.group(1);
            String sectionPart = matcher.group(2);
            
            log.debug("Parsed class name: {} -> class='{}', section='{}'", trimmed, classpart, sectionPart);
            return new ParsedClassName(classpart, sectionPart);
        }
        
        // If no match, try some heuristics
        
        // Check for format like "10A" (digit followed by letter)
        Pattern digitLetterPattern = Pattern.compile("^(\\d+)([A-Z]+)$", Pattern.CASE_INSENSITIVE);
        Matcher digitLetterMatcher = digitLetterPattern.matcher(trimmed);
        if (digitLetterMatcher.matches()) {
            String classPart = digitLetterMatcher.group(1);
            String sectionPart = digitLetterMatcher.group(2);
            log.debug("Parsed class name (digit-letter): {} -> class='{}', section='{}'", trimmed, classPart, sectionPart);
            return new ParsedClassName(classPart, sectionPart);
        }
        
        // Check for format like "LKG" or "UKG" (assume default section A)
        if (trimmed.matches("^[A-Z\\.]+$")) {
            log.debug("Parsed class name (class only): {} -> class='{}', section='A' (default)", trimmed, trimmed);
            return new ParsedClassName(trimmed, "A");
        }
        
        // If all else fails, treat the whole thing as class name with default section A
        log.warn("Could not parse class name format: {}. Using entire string as class name with default section A", trimmed);
        return new ParsedClassName(trimmed, "A");
    }
    
    /**
     * Get the full formatted class name (CLASS-SECTION)
     * @param input The input string
     * @return Formatted full name
     */
    public static String getFullName(String input) {
        return parse(input).getFullName();
    }
    
    /**
     * Get just the class part
     * @param input The input string
     * @return Class part only
     */
    public static String getClassPart(String input) {
        return parse(input).getClassName();
    }
    
    /**
     * Get just the section part
     * @param input The input string
     * @return Section part only
     */
    public static String getSectionPart(String input) {
        return parse(input).getSection();
    }
}

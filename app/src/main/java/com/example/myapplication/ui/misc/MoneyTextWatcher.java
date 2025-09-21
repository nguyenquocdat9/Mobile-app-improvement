package com.example.myapplication.ui.misc;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MoneyTextWatcher implements TextWatcher {
    private final EditText editText;
    private final DecimalFormat decimalFormat;
    private boolean isUpdating = false;
    private final int maxDecimalDigits;
    private String current = "";

    /**
     * Constructor for MoneyTextWatcher
     * @param editText EditText to apply the formatting to
     * @param maxDecimalDigits maximum number of decimal digits allowed
     */
    public MoneyTextWatcher(EditText editText, int maxDecimalDigits) {
        this.editText = editText;
        this.maxDecimalDigits = maxDecimalDigits;
        this.decimalFormat = new DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.US));
    }

    /**
     * Constructor with default 2 decimal digits
     * @param editText EditText to apply the formatting to
     */
    public MoneyTextWatcher(EditText editText) {
        this(editText, 3);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Not needed for this implementation
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Not needed for this implementation
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (isUpdating) {
            return;
        }

        String originalText = s.toString();

        // If the text hasn't changed, no need to update
        if (originalText.equals(current)) {
            return;
        }

        // Save cursor position
        int cursorPosition = editText.getSelectionStart();

        isUpdating = true;

        // If the text is empty, reset and return
        if (originalText.isEmpty()) {
            current = "";
            isUpdating = false;
            return;
        }

        // Remove all formatting characters
        String cleanString = originalText.replaceAll("[^\\d.]", "");

        // Check if the input text has a decimal point
        boolean hasDecimalPoint = originalText.contains(".");

        // Handle case when user enters just a decimal point
        if (cleanString.equals(".")) {
            cleanString = "0.";
        }

        // Handle multiple decimal points by keeping only the first one
        int firstDecimalPosition = cleanString.indexOf(".");
        if (firstDecimalPosition != -1) {
            String beforeDecimal = cleanString.substring(0, firstDecimalPosition);
            String afterDecimal = cleanString.substring(firstDecimalPosition + 1).replaceAll("\\.", "");

            // Limit decimal digits
            if (afterDecimal.length() > maxDecimalDigits) {
                afterDecimal = afterDecimal.substring(0, maxDecimalDigits);
            }

            cleanString = beforeDecimal + "." + afterDecimal;
        }

        // Format the string with commas
        try {
            String formattedNumber;

            if (cleanString.contains(".")) {
                // Split the number at decimal point
                String[] parts = cleanString.split("\\.");

                // Handle integer part
                String integerPart = parts[0];
                if (integerPart.isEmpty()) integerPart = "0";

                // Format integer part with commas
                formattedNumber = integerPart.isEmpty() ? "0" : formatWithCommas(integerPart);

                // Add decimal part if exists
                if (parts.length > 1) {
                    formattedNumber += "." + parts[1];
                } else {
                    formattedNumber += ".";
                }
            } else {
                formattedNumber = formatWithCommas(cleanString);
            }

            // Calculate new cursor position
            int newCursorPosition = calculateNewCursorPosition(originalText, formattedNumber, cursorPosition);

            // Update current value
            current = formattedNumber;

            // Set the formatted text
            editText.removeTextChangedListener(this);
            editText.setText(formattedNumber);

            // Set cursor position
            if (newCursorPosition > formattedNumber.length()) {
                newCursorPosition = formattedNumber.length();
            }
            editText.setSelection(newCursorPosition);
            editText.addTextChangedListener(this);

        } catch (NumberFormatException e) {
            // In case of error, use clean string
            editText.removeTextChangedListener(this);
            editText.setText(cleanString);

            // Set cursor to a safe position
            int safePosition = Math.min(cursorPosition, cleanString.length());
            editText.setSelection(safePosition);

            current = cleanString;
            editText.addTextChangedListener(this);
        }

        isUpdating = false;
    }

    /**
     * Format a number string with commas for thousands separators
     */
    private String formatWithCommas(String numberString) {
        if (numberString == null || numberString.isEmpty()) {
            return "0";
        }

        // Remove any existing non-digit characters
        numberString = numberString.replaceAll("[^\\d]", "");

        // Convert to BigDecimal and format
        try {
            BigDecimal number = new BigDecimal(numberString);
            return decimalFormat.format(number);
        } catch (NumberFormatException e) {
            return numberString;
        }
    }

    /**
     * Calculate new cursor position after formatting
     */
    private int calculateNewCursorPosition(String originalText, String formattedText, int originalCursorPos) {
        // Handle out of bounds positions
        if (originalCursorPos > originalText.length()) {
            return formattedText.length();
        }

        // Count meaningful characters before cursor in original text
        int countDigitsBeforeCursor = 0;
        for (int i = 0; i < originalCursorPos; i++) {
            char c = originalText.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                countDigitsBeforeCursor++;
            }
        }

        // Find the position in formatted text that corresponds to that many digits
        int newPos = 0;
        int countDigits = 0;

        for (int i = 0; i < formattedText.length(); i++) {
            char c = formattedText.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                countDigits++;
            }

            if (countDigits > countDigitsBeforeCursor) {
                break;
            }

            newPos = i + 1;
        }

        // Special case: if cursor is after a decimal point in original text
        if (originalCursorPos > 0 && originalText.charAt(originalCursorPos - 1) == '.') {
            for (int i = 0; i < formattedText.length(); i++) {
                if (formattedText.charAt(i) == '.') {
                    newPos = i + 1;
                    break;
                }
            }
        }

        return newPos;
    }

    /**
     * Get the numeric value without formatting
     * @return double value of the formatted text
     */
    public double getNumericValue() {
        String cleanString = editText.getText().toString().replaceAll("[^\\d.]", "");
        try {
            return Double.parseDouble(cleanString);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Get the numeric value as BigDecimal without formatting
     * @return BigDecimal value of the formatted text
     */
    public BigDecimal getBigDecimalValue() {
        String cleanString = editText.getText().toString().replaceAll("[^\\d.]", "");
        try {
            return new BigDecimal(cleanString);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
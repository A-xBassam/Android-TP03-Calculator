package com.example.myapplication02;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private TextView tvDisplay;
    private final StringBuilder currentInput = new StringBuilder("0");
    private boolean isResultShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvDisplay = findViewById(R.id.tvDisplay);

        setupButtonListeners();
    }

    private void setupButtonListeners() {
        int[] buttonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnAdd, R.id.btnSub, R.id.btnMul, R.id.btnDiv,
                R.id.btnDot, R.id.btnOpenBracket, R.id.btnCloseBracket
        };

        View.OnClickListener clickListener = v -> {
            Button b = (Button) v;
            String val = b.getText().toString();

            if (isResultShown) {
                if (isOperator(val)) {
                    isResultShown = false;
                } else {
                    currentInput.setLength(0);
                    isResultShown = false;
                }
            }

            if (currentInput.toString().equals("0") && !isOperator(val) && !val.equals(".")) {
                currentInput.setLength(0);
            }

            currentInput.append(val);
            tvDisplay.setText(currentInput.toString());
        };

        for (int id : buttonIds) {
            findViewById(id).setOnClickListener(clickListener);
        }

        // Clear button
        findViewById(R.id.btnClear).setOnClickListener(v -> {
            currentInput.setLength(0);
            currentInput.append("0");
            isResultShown = false;
            tvDisplay.setText(currentInput.toString());
        });

        // Backspace button
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (isResultShown) {
                currentInput.setLength(0);
                currentInput.append("0");
                isResultShown = false;
            } else if (currentInput.length() > 1) {
                currentInput.deleteCharAt(currentInput.length() - 1);
            } else {
                currentInput.setLength(0);
                currentInput.append("0");
            }
            tvDisplay.setText(currentInput.toString());
        });

        // Equal button
        findViewById(R.id.btnEqual).setOnClickListener(v -> {
            String expr = currentInput.toString();
            try {
                double result = eval(expr);
                if (result == (long) result) {
                    currentInput.setLength(0);
                    currentInput.append(String.valueOf((long) result));
                } else {
                    currentInput.setLength(0);
                    currentInput.append(String.valueOf(result));
                }
                tvDisplay.setText(currentInput.toString());
                isResultShown = true;
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Invalid Expression", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isOperator(String val) {
        return val.equals("+") || val.equals("-") || val.equals("*") || val.equals("/");
    }

    // Simple expression evaluator using recursive descent parser
    private double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                while (true) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                while (true) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) {
                        double divisor = parseFactor();
                        if (divisor == 0) throw new ArithmeticException("Division by zero");
                        x /= divisor; // division
                    }
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                return x;
            }
        }.parse();
    }
}

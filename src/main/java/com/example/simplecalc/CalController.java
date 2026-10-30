//eto major code file

package com.example.simplecalc;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Controller
public class CalController {
//pang display to, httpsession and model used to state the floating display sa calcu
    @GetMapping("/")
    public String showCalc(HttpSession session, Model model) {
        if (session.getAttribute("display") == null) {
            session.setAttribute("display", "0");
        }
        //pang get ng kung ano mang nakadisplay na number or error
        model.addAttribute("display", session.getAttribute("display"));
        model.addAttribute("error", session.getAttribute("error"));

        return "calculator";
    }
    //ito yung mismong vcalulator logics
    @PostMapping("/calculate")
    public String calculate(
            @RequestParam String action,
            @RequestParam(required = false) String value,
            HttpSession session,
            Model model) {
        //ito if number laman
        if (action.equals("number")) {
            //ginagawang String para mabasa
            String display = (String) session.getAttribute("display");
            //kung hindi empty si operator = tatanggapin ung 2nd number na ieenter
            if (session.getAttribute("operator") != null) {
                session.setAttribute("secondNumberEntered", true);
            }
            //kung wala pang naka display na number, degfault sa 0
            if (display == null) {
                display = "0";
            }
            //if 0 ang display, matic irereplace ng value
            if (display.equals("0")) {
                display = value;
            } else {
                //default lang na kapag hindi zero magdidkit lang. ex. 5 display, type ka ng 5, magiging 55 instead of 5 lang ulit
                display = display + value;
            }
            session.setAttribute("display", display);
        }
        //eto kapag clinic ung C sa button, mareremove lahat
        if (action.equals("clear")) {
            session.setAttribute("display", "0");
            session.removeAttribute("firstNumber");
            session.removeAttribute("operator");
            session.removeAttribute("error");
            session.removeAttribute("secondNumberEntered");
        }
        //ito for decimal stuff
        if (action.equals("decimal")) {
            String display = (String) session.getAttribute("display");
            //ganung ulit kapag wala laman, zero display
            if (display == null) {
                display = "0";
            }
            // kapag walang decimal tas nag click ng decimal, maglalagay ng decimal, else hindi mapapalitan, anti multiple decimal to
            if (!display.contains(".")) {
                display = display + ".";
            }

            session.setAttribute("display", display);
        }
        if (action.equals("negate")) {
            String display = (String) session.getAttribute("display");
            if (display == null) {
                display = "0";
            }
            // walang -0, kaya pag zero wala gagawin
            if (Double.parseDouble(display) != 0) {
                if (display.startsWith("-")) {
                    display = display.substring(1);   // tanggalin yung -
                } else {
                    display = "-" + display;          // lagyan ng -
                }
            }
            session.setAttribute("display", display);
        }
        // percent
        if (action.equals("percent")) {
            String display = (String) session.getAttribute("display");
            if (display == null) {
                display = "0";
            }
            String first = (String) session.getAttribute("firstNumber");
            String operator = (String) session.getAttribute("operator");
            double current = Double.parseDouble(display);
            double result;

            if (first != null && operator != null
                    && Boolean.TRUE.equals(session.getAttribute("secondNumberEntered"))
                    && (operator.equals("+") || operator.equals("-"))) {
                // 200 + 10% -> 10% ng 200 = 20
                result = Double.parseDouble(first) * current / 100;
            } else {
                // 50% -> 0.5, at 200 * 10% -> 200 * 0.1
                result = current / 100;
            }
            session.setAttribute("display", format(result));
        }
        if (action.equals("backspace")) {
            String display = (String) session.getAttribute("display");
            if (display == null) {
                display = "0";
            }

            display = display.substring(0, display.length() - 1);

            // kung wala nang natira, or "-" or "-0" na lang, balik sa 0
            if (display.isEmpty() || display.equals("-") || display.equals("-0")) {
                display = "0";
            }

            session.setAttribute("display", display);
        }
        // ito yung operators na
        if (action.equals("operator")) {
            String display = (String) session.getAttribute("display");
            String pendingOp = (String) session.getAttribute("operator");
            String first = (String) session.getAttribute("firstNumber");

            if (pendingOp != null && Boolean.TRUE.equals(session.getAttribute("secondNumberEntered"))) {
                // may naunang operation na, i-solve muna tapos gawing num1
                String result = compute(first, pendingOp, display, session);
                session.setAttribute("firstNumber", result);
            } else if (pendingOp == null) {
                // first operator pa lang
                session.setAttribute("firstNumber", display);
            }
            // kung may operator na pero wala pang 2nd number, papalitan lang yung operator

            session.setAttribute("operator", value);
            session.setAttribute("secondNumberEntered", false);
            session.setAttribute("display", "0");
        }
        //equals for the actual stuff happening
        if (action.equals("equals")) {
            String first = (String) session.getAttribute("firstNumber");
            String operator = (String) session.getAttribute("operator");
            String second = (String) session.getAttribute("display");

            if (first != null && operator != null) {
                if (Boolean.TRUE.equals(session.getAttribute("secondNumberEntered"))) {
                    session.setAttribute("display", compute(first, operator, second, session));
                } else {
                    session.setAttribute("display", first);
                }
            }

            // tapos na yung operation, linisin para hindi madoble
            session.removeAttribute("firstNumber");
            session.removeAttribute("operator");
            session.setAttribute("secondNumberEntered", false);
        }
        //basta ito ididisplay yung final outout
        model.addAttribute("display", session.getAttribute("display"));
        model.addAttribute("error", session.getAttribute("error"));

        return "calculator";
    }
    private String format(double number) {
        return BigDecimal.valueOf(number)
                .setScale(8, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }
    private String compute(String first, String operator, String second, HttpSession session) {
        double num1 = Double.parseDouble(first);
        double num2 = Double.parseDouble(second);

        if (operator.equals("/") && num2 == 0) {
            session.setAttribute("error", "Cannot divide by zero!");
            return "0";
        }
        session.removeAttribute("error");

        double result = 0;
        switch (operator) {
            case "+": result = num1 + num2; break;
            case "-": result = num1 - num2; break;
            case "*": result = num1 * num2; break;
            case "/": result = num1 / num2; break;
        }

        return format(result);
    }
}

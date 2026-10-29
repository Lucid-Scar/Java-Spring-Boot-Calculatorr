//eto major code file

package com.example.simplecalc;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        // ito yung operators na
        if (action.equals("operator")) {
            String display = (String) session.getAttribute("display");
            //nagset ng new attributes para sa switch case
            session.setAttribute("firstNumber", display);
            session.setAttribute("operator", value);
            session.setAttribute("secondNumberEntered", false);

            session.setAttribute("display", "0");
        }
        //equals for the actual stuff happening
        if (action.equals("equals")) {
            //pansin mo getAttribute na, kanina setAttribute, sheesh getter setter
            String first = (String) session.getAttribute("firstNumber");
            String operator = (String) session.getAttribute("operator");
            String second = (String) session.getAttribute("display");

            //validation
            if (first == null || operator == null) {
            //another validation
            } else if (second == null ||
                    !Boolean.TRUE.equals(session.getAttribute("secondNumberEntered"))) {
                session.setAttribute("display", first);
            //validation, strict na double lang tinatanggap
            } else {
                double num1 = Double.parseDouble(first);
                double num2 = Double.parseDouble(second);
                //lkatamad na mag comment pre gets mo na to
                if (operator.equals("/") && num2 == 0) {
                    session.setAttribute("error", "Cannot divide by zero!");
                    session.setAttribute("display", "0");

                } else {
                    session.removeAttribute("error");

                    double result = 0;

                    switch (operator) {
                        case "+":
                            result = num1 + num2;
                            break;

                        case "-":
                            result = num1 - num2;
                            break;

                        case "*":
                            result = num1 * num2;
                            break;

                        case "/":
                            result = num1 / num2;
                            break;
                    }

                    session.setAttribute("display", String.valueOf(result));
                }
            }
        }       //basta ito ididisplay yung final outout
            model.addAttribute("display", session.getAttribute("display"));
        model.addAttribute("error", session.getAttribute("error"));

        return "calculator";
    }
}

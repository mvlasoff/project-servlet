package com.tictactoe;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "LogicServlet", value = "/logic")
public class LogicServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        //get current session
        HttpSession currentSession = req.getSession();

        //get Field object
        Field field = extractField(currentSession);

        //get index of cell where click happened
        int index = getSelectedIndex(req);
        Sign currentSign = field.getField().get(index);

        //check if cell was empty
        //otherwise forward user to the main page
        if(Sign.EMPTY != currentSign) {
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
            dispatcher.forward(req, resp);
            return;
        }

        //put cross where user clicked
        field.getField().put(index, Sign.CROSS);

        //check if cross won
        if(checkWin(resp, currentSession, field)) {
            return;
        }

        //get empty cell and put nought
        int emptyFieldIndex = field.getEmptyFieldIndex();
        if(emptyFieldIndex >= 0) {
            field.getField().put(emptyFieldIndex, Sign.NOUGHT);
            //check if nought won
            if(checkWin(resp, currentSession, field)) {
                return;
            }
        } else {
            //when no empty cell then draw occurred
            currentSession.setAttribute("draw", true);
        }

        //get list of signs
        List<Sign> data = field.getFieldData();

        //save list of signs and field object in the session
        currentSession.setAttribute("data", data);
        currentSession.setAttribute("field", field);

        resp.sendRedirect("/index.jsp");
    }

    private Field extractField(HttpSession currentSession) {
        Object fieldAttribute = currentSession.getAttribute("field");
        if (Field.class != fieldAttribute.getClass()) {
            currentSession.invalidate();
            throw new RuntimeException("Session is broken, try one more time");
        }
        return (Field) fieldAttribute;
    }

    private int getSelectedIndex(HttpServletRequest req) {
        String click = req.getParameter("click");
        boolean isNumeric = click.chars().allMatch(Character::isDigit);
        return isNumeric ? Integer.parseInt(click) : 0;
    }

    private boolean checkWin(HttpServletResponse response, HttpSession currentSession, Field field) throws IOException {
        Sign winner = field.checkWin();
        if(Sign.CROSS == winner || Sign.NOUGHT == winner) {
            //add flag showing presence of winner
            currentSession.setAttribute("winner", winner);

            //get list of signs
            List<Sign> data = field.getFieldData();

            //save the list in session
            currentSession.setAttribute("data", data);

            response.sendRedirect("/index.jsp");
            return true;
        }
        return false;
    }
}

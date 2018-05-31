/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.rootbank.web;

import cz.fi.muni.pv168.bank.Person;
import cz.fi.muni.pv168.bank.PersonManager;
import cz.fi.muni.pv168.common.ServiceFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Servlet for managing persons.
 *
 * @author Aneta Moravcikova, Katarina Matusova
 */
@WebServlet(PersonsServlet.URL_MAPPING + "/*")
public class PersonsServlet extends HttpServlet {

    private static final String LIST_JSP = "/list.jsp";
    public static final String URL_MAPPING = "/persons";

    private final static Logger log = LoggerFactory.getLogger(PersonsServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("GET ...");
        showPersonsList(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("utf-8");
    
        String action = request.getPathInfo();
        log.debug("POST ... {}",action);
        LocalDate dateBorn = null;
        switch (action) {
            case "/add":
                
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                String name = request.getParameter("name");
                String birthDate = request.getParameter("born");
                Date born = null;
                
                if (name == null || name.length() == 0 || birthDate == null || birthDate.length() == 0 ) {
                    request.setAttribute("chyba", "Je nutné vyplnit všechny hodnoty !");
                    log.debug("form data invalid");
                    showPersonsList(request, response);
                    return;
                }
                try {
                        born = sdf.parse(birthDate);
                } catch (ParseException e) {
                    log.error("cannot parse date", e);
                    e.printStackTrace();
                }
                if(born == null ){
                    request.setAttribute("chyba", "zly format datumu (napr.: 4.2.2042)");
                    showPersonsList(request, response);
                    return;
                }
                dateBorn = born.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                try {
                    Person person= new Person();
                    person.setName(name);
                    person.setBorn(dateBorn);
                    getPersonManager().createPerson(person);

                    log.debug("redirecting after POST");
                    response.sendRedirect(request.getContextPath()+URL_MAPPING);
                    return;
                } catch (Exception e) {
                    log.error("Cannot add person", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
            case "/delete":
                try {
                    Long id = Long.valueOf(request.getParameter("id"));
                    Person person = getPersonManager().getPerson(id);
                    if (person != null) {
                        getPersonManager().deletePerson(person);
                        response.sendRedirect(request.getContextPath() + URL_MAPPING);
                        return;
                    } else {
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Person with id " + id + " doesn't exist");
                        return;
                    }
                } catch (ServiceFailureException | IllegalArgumentException e) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    return;
                }
            case "/update":
                //TODO
                return;
            default:
                log.error("Unknown action " + action);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown action " + action);
        }
    }

    /**
     * Gets PersonManager from ServletContext, where it was stored by {@link StartListener}.
     *
     * @return PersonManager instance
     */
    private PersonManager getPersonManager() {
        return (PersonManager) getServletContext().getAttribute("personManager");
    }

    /**
     * Stores the list of persons to request attribute "persons" and forwards to the JSP to display it.
     */
    private void showPersonsList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            log.debug("showing table of persons");
            request.setAttribute("persons", getPersonManager().findAllPeople());
            request.getRequestDispatcher(LIST_JSP).forward(request, response);
        } catch (Exception e) {
            log.error("Cannot show persons", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}


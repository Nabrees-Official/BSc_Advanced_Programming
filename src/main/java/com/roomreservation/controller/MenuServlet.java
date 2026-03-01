package com.roomreservation.controller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class MenuServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            resp.sendRedirect("login.html");
            return;
        }

        String username = session.getAttribute("username").toString();
        String role = String.valueOf(session.getAttribute("role"));

        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        resp.getWriter().println("""
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Dashboard</title>

                    <style>
                        *{
                            margin:0;
                            padding:0;
                            box-sizing:border-box;
                            font-family:'Segoe UI', Arial, sans-serif;
                        }

                        body{
                            min-height:100vh;
                            background: linear-gradient(135deg, #f4f7ff, #e8f0fe);
                            padding: 30px 18px;
                        }

                        .container{
                            max-width: 1100px;
                            margin: 0 auto;
                            animation: fadeIn .5s ease-in-out;
                        }

                        @keyframes fadeIn{
                            from{opacity:0; transform: translateY(20px);}
                            to{opacity:1; transform: translateY(0);}
                        }

                        /* ✅ centered header */
                        .header{
                            background:white;
                            padding: 26px 24px;
                            border-radius: 16px;
                            box-shadow: 0 15px 40px rgba(0,0,0,0.08);
                            margin-bottom: 20px;

                            text-align: center;        /* ✅ center text */
                        }

                        .header h2{
                            color:#1e3a8a;
                            margin-bottom: 8px;
                            font-size: 26px;
                            font-weight: 800;
                        }

                        .header p{
                            color:#6b7280;
                            font-size:14px;
                        }

                        /* ✅ Perfect alignment grid */
                        .cards{
                            display:grid;
                            grid-template-columns: repeat(3, 1fr);
                            gap: 18px;
                            align-items: stretch;
                        }

                        .card{
                            background:white;
                            padding: 22px;
                            border-radius: 16px;
                            text-align:center;
                            box-shadow:0 15px 40px rgba(0,0,0,0.06);
                            transition: .25s ease;
                            text-decoration:none;
                            color:#111827;

                            min-height: 140px;
                            display:flex;
                            flex-direction:column;
                            justify-content:center;
                        }

                        .card:hover{
                            transform: translateY(-4px);
                            box-shadow:0 20px 50px rgba(0,0,0,0.10);
                        }

                        .card h3{
                            margin-bottom:8px;
                            color:#2563eb;
                            font-size: 18px;
                        }

                        .card p{
                            font-size: 13px;
                            color:#6b7280;
                            line-height: 1.45;
                        }

                        .logout{
                            margin-top: 22px;
                            display:flex;
                            justify-content:center;
                        }

                        .logout a{
                            display:inline-block;
                            padding:10px 20px;
                            background:#ef4444;
                            color:white;
                            border-radius:10px;
                            text-decoration:none;
                            font-weight:700;
                            transition:.25s;
                            box-shadow:0 10px 22px rgba(239,68,68,0.22);
                        }

                        .logout a:hover{
                            background:#b91c1c;
                            transform: translateY(-2px);
                        }

                        /* ✅ Responsive */
                        @media (max-width: 920px){
                            .cards{grid-template-columns: repeat(2, 1fr);}
                        }
                        @media (max-width: 560px){
                            .cards{grid-template-columns: 1fr;}
                            body{padding: 18px 12px;}
                            .header{padding: 18px;}
                            .header h2{font-size: 22px;}
                        }
                    </style>
                </head>

                <body>
                <div class="container">

                    <div class="header">
                        <h2>Room Reservation Dashboard</h2>
                        <p>Welcome, <b>%s</b> (%s)</p>
                    </div>

                    <div class="cards">
                        <a href="addReservation.html" class="card">
                            <h3>Add Reservation</h3>
                            <p>Create a new booking for guests.</p>
                        </a>

                        <a href="viewReservation.html" class="card">
                            <h3>View Reservation</h3>
                            <p>Search and view reservation details.</p>
                        </a>

                        <a href="calculateBill.html" class="card">
                            <h3>Calculate Bill</h3>
                            <p>Generate total cost for a reservation.</p>
                        </a>

                        <a href="listReservations" class="card">
                            <h3>All Reservations</h3>
                            <p>View complete reservation records.</p>
                        </a>

                        <a href="help.html" class="card">
                            <h3>Help Guide</h3>
                            <p>Instructions for new staff members.</p>
                        </a>

                        <a href="logout" class="card" style="border:1px solid rgba(239,68,68,0.25);">
                            <h3 style="color:#ef4444;">Quick Logout</h3>
                            <p>End your session safely.</p>
                        </a>
                    </div>

                    <div class="logout">
                        <a href="logout">Logout</a>
                    </div>

                </div>
                </body>
                </html>
                """.formatted(username, role));
    }
}
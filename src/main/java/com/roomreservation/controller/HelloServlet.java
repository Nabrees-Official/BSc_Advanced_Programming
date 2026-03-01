package com.roomreservation.controller;

import com.roomreservation.util.DBConnection;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;

public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        boolean ok;
        String dbStatus;

        try (Connection con = DBConnection.getConnection()) {
            ok = true;
            dbStatus = "MySQL Connected Successfully!";
        } catch (Exception e) {
            ok = false;
            dbStatus = "MySQL Connection Failed: " + e.getMessage();
        }

        String boxBg = ok ? "#ecfdf5" : "#fef2f2";
        String boxBorder = ok ? "#86efac" : "#fecaca";
        String boxText = ok ? "#065f46" : "#991b1b";
        String icon = ok ? "✅" : "❌";
        String title = ok ? "System Status" : "System Status (Issue)";

        resp.getWriter().println("""
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>%s</title>

                  <style>
                    *{margin:0;padding:0;box-sizing:border-box;font-family:'Segoe UI',Arial,sans-serif;}
                    body{
                      min-height:100vh;
                      display:flex;
                      justify-content:center;
                      align-items:center;
                      background: linear-gradient(135deg, #f4f7ff, #e8f0fe);
                      padding:20px;
                      color:#111827;
                    }
                    .card{
                      width:min(900px, 100%%);
                      background:#fff;
                      border-radius:16px;
                      padding:28px;
                      box-shadow:0 18px 55px rgba(0,0,0,0.08);
                      animation: fadeIn .5s ease-in-out;
                      text-align:center;
                    }
                    @keyframes fadeIn{
                      from{opacity:0; transform:translateY(18px);}
                      to{opacity:1; transform:translateY(0);}
                    }

                    h2{
                      color:#1e3a8a;
                      font-size:26px;
                      margin-bottom:8px;
                      font-weight:800;
                    }
                    p{
                      color:#6b7280;
                      font-size:14px;
                      line-height:1.6;
                    }

                    .status{
                      margin-top:14px;
                      padding:14px;
                      border-radius:14px;
                      background:%s;
                      border:1px solid %s;
                      color:%s;
                      font-weight:800;
                      line-height:1.6;
                    }

                    .actions{
                      margin-top:18px;
                      display:flex;
                      gap:10px;
                      justify-content:center;
                      flex-wrap:wrap;
                    }

                    .btn{
                      display:inline-block;
                      padding:10px 16px;
                      border-radius:10px;
                      text-decoration:none;
                      font-weight:800;
                      font-size:14px;
                      transition:.25s;
                      border:1px solid transparent;
                    }

                    .btn-primary{
                      background:#2563eb;
                      color:white;
                      box-shadow:0 10px 22px rgba(37,99,235,0.18);
                    }
                    .btn-primary:hover{
                      background:#1e40af;
                      transform:translateY(-2px);
                    }

                    .btn-light{
                      background:#ffffff;
                      color:#2563eb;
                      border-color:#c7d2fe;
                    }
                    .btn-light:hover{
                      background:#f1f5ff;
                      transform:translateY(-2px);
                    }
                  </style>
                </head>

                <body>
                  <div class="card">

                    <h2>%s</h2>
                    <p>This page confirms the application is running and checks the database connection.</p>

                    <div class="status">%s %s</div>

                    <div class="actions">
                      <a class="btn btn-primary" href="menu">Back to Menu</a>
                      <a class="btn btn-light" href="help.html">Help</a>
                      <a class="btn btn-light" href="hello">Refresh</a>
                    </div>

                  </div>
                </body>
                </html>
                """.formatted(
                title,
                boxBg, boxBorder, boxText,
                title,
                icon, dbStatus
        ));
    }
}
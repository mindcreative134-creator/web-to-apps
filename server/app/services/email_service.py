import smtplib
import os
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from app.config import get_settings

settings = get_settings()

class EmailService:
    @staticmethod
    def send_verification_email(to_email: str, code: str):
        """
        Sends a verification email with a code.
        Note: This is a stub that logs to console if SMTP is not configured.
        """
        subject = "WebToApp Account Verification"
        body = f"Your verification code is: {code}\n\nPlease enter this in the app to verify your account."
        
        # Check if SMTP is configured (mock check for now)
        smtp_server = os.getenv("SMTP_SERVER")
        smtp_port = os.getenv("SMTP_PORT", 587)
        smtp_user = os.getenv("SMTP_USER")
        smtp_pass = os.getenv("SMTP_PASS")
        
        if not smtp_server or not smtp_user:
            print(f"EmailService: SMTP not configured. Mock Email to {to_email}: {body}")
            return True

        try:
            msg = MIMEMultipart()
            msg['From'] = smtp_user
            msg['To'] = to_email
            msg['Subject'] = subject
            msg.attach(MIMEText(body, 'plain'))

            server = smtplib.SMTP(smtp_server, int(smtp_port))
            server.starttls()
            server.login(smtp_user, smtp_pass)
            server.send_message(msg)
            server.quit()
            return True
        except Exception as e:
            print(f"EmailService: Failed to send email to {to_email}: {e}")
            return False

email_service = EmailService()

from passlib.context import CryptContext

pwd_context = CryptContext(schemes=["pbkdf2_sha256"], deprecated="auto")

def verify_password(plain_password: str, hashed_password: str) -> bool:
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password: str) -> str:
    return pwd_context.hash(password)

def decrypt_password(encrypted_password: str) -> str:
    # This is a legacy placeholder. In this app, we use hashing, not encryption.
    return "legacy_encrypted"

def encrypt_password(password: str) -> str:
    # Placeholder for backward compatibility if needed, but we use get_password_hash
    return f"encrypted({password})"

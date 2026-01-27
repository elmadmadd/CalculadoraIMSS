import os
import platform
import threading
import time

class SoundManager:
    """Gestor de sonidos para feedback auditivo con fallback seguro"""
    def __init__(self, page):
        self.page = page
        self.audios = {}
        self.enabled = False
        self.is_playing = False
        self.use_pygame = False

        self.system = platform.system()

        # Intentar usar pygame (más seguro y multiplataforma)
        try:
            import pygame
            pygame.mixer.init(frequency=22050, size=-16, channels=2, buffer=512)
            self.use_pygame = True
            print(f"✓ SoundManager: Modo pygame activado para {self.system}")
        except ImportError:
            print(f"⚠ pygame no disponible, usando modo nativo para {self.system}")
            self.use_pygame = False
        except Exception as e:
            print(f"⚠ Error inicializando pygame: {e}, usando modo nativo")
            self.use_pygame = False

        self.audios = {
            "pop": "pop.mp3",
            "cash": "cash.mp3",
            "error": "pop.mp3",
            "wrong": "wrong.mp3"
        }
        self.enabled = True

    def play(self, name):
        """Reproduce sonido usando pygame (seguro) o fallback nativo"""
        if not self.enabled or name not in self.audios:
            return

        try:
            file_path = f"assets/{self.audios[name]}"

            if self.use_pygame:
                # MODO PYGAME (SEGURO - SIN COMANDOS DEL SISTEMA)
                import pygame
                import threading

                def play_async():
                    try:
                        pygame.mixer.music.load(file_path)
                        pygame.mixer.music.play()
                        # Esperar a que termine (no bloqueante)
                        while pygame.mixer.music.get_busy():
                            import time
                            time.sleep(0.1)
                    except Exception as e:
                        print(f"Error pygame: {e}")

                threading.Thread(target=play_async, daemon=True).start()

            else:
                # FALLBACK NATIVO (solo si pygame no está disponible)
                import time
                import threading

                if self.system == "Darwin":  # macOS
                    def run_sound():
                        duration = "3" if name == "cash" else "0.5"
                        os.system(f"afplay '{file_path}' -t {duration}")
                        time.sleep(0.1)
                    threading.Thread(target=run_sound, daemon=True).start()

                elif self.system == "Windows":
                    # Usar winsound (biblioteca estándar de Python, más segura)
                    try:
                        import winsound
                        def play_win():
                            winsound.PlaySound(file_path, winsound.SND_FILENAME | winsound.SND_ASYNC)
                        threading.Thread(target=play_win, daemon=True).start()
                    except Exception:
                        # Si winsound falla, silenciar (no usar PowerShell)
                        pass

        except Exception as e:
            print(f"Error audio: {e}")

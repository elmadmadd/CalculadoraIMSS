from core.agenda_manager import AgendaManager
# Helper to bridge the interface between AgendaView (which calls obtener_stats)
# and AgendaManager (which has obtener_estadisticas)

class AgendaManagerAdapter(AgendaManager):
    def obtener_stats(self):
        # Maps obtener_estadisticas structure to what AgendaView expects
        stats = self.obtener_estadisticas()
        result = {
            'total': stats['total'],
            'prospectos': stats['por_estatus'].get('Prospecto', 0),
            'en_proceso': stats['por_estatus'].get('En Proceso', 0),
            'aprobados': stats['por_estatus'].get('Aprobado', 0)
        }
        return result

    def agregar_cliente(self, datos):
        # Override to ensure integer ID is returned if needed, though base is fine
        return super().agregar_cliente(datos)

    # Re-export base methods explicitly if needed by other consumers,
    # but inheritance handles it.

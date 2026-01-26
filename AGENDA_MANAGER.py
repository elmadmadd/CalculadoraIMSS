"""
AGENDA_MANAGER.py
Sistema de gestión de clientes para asesores financieros.
Permite agregar, editar, buscar y gestionar una base de datos local de clientes.
"""

import sqlite3
import json
from datetime import datetime
from pathlib import Path
from typing import List, Dict, Optional

class AgendaManager:
    """Gestor de agenda de clientes con persistencia en SQLite"""
    
    def __init__(self, db_path: str = "agenda_clientes.db"):
        """
        Inicializa el gestor de agenda.
        
        Args:
            db_path: Ruta al archivo de base de datos SQLite
        """
        self.db_path = db_path
        self._init_database()
    
    def _init_database(self):
        """Crea las tablas necesarias si no existen y realiza migraciones"""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        # Tabla de clientes
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS clientes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                telefono TEXT,
                email TEXT,
                nss TEXT,
                empresa TEXT,
                puesto TEXT,
                salario REAL,
                notas TEXT,
                estatus TEXT DEFAULT 'Prospecto',
                grupo TEXT,
                fecha_registro TEXT NOT NULL,
                fecha_actualizacion TEXT NOT NULL,
                asesor TEXT
            )
        ''')

        # Tabla de grupos
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS grupos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL UNIQUE
            )
        ''')
        
        # Insertar grupos por defecto si la tabla está vacía
        cursor.execute("SELECT COUNT(*) FROM grupos")
        if cursor.fetchone()[0] == 0:
            grupos_default = ["IMSS", "SNTE 23", "SNTE 14", "IEPOO", "PEMEX", "GOBIERNO"]
            cursor.executemany("INSERT INTO grupos (nombre) VALUES (?)", [(g,) for g in grupos_default])
        
        # Migración: Añadir columna 'grupo' a clientes si no existe
        try:
            cursor.execute("ALTER TABLE clientes ADD COLUMN grupo TEXT")
        except sqlite3.OperationalError:
            pass # La columna ya existe
            
        # Tabla de interacciones/seguimiento
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS interacciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cliente_id INTEGER NOT NULL,
                fecha TEXT NOT NULL,
                tipo TEXT NOT NULL,
                descripcion TEXT,
                FOREIGN KEY (cliente_id) REFERENCES clientes (id)
            )
        ''')
        
        conn.commit()
        conn.close()
    
    # --- Gestión de Grupos ---
    def obtener_grupos(self) -> List[str]:
        """Obtiene la lista de nombres de grupos disponibles"""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        cursor.execute("SELECT nombre FROM grupos ORDER BY nombre")
        grupos = [row[0] for row in cursor.fetchall()]
        conn.close()
        return grupos

    def agregar_grupo(self, nombre: str) -> bool:
        """Agrega un nuevo grupo. Retorna True si se agregó, False si ya existía."""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        try:
            cursor.execute("INSERT INTO grupos (nombre) VALUES (?)", (nombre.strip().upper(),))
            conn.commit()
            return True
        except sqlite3.IntegrityError:
            return False
        finally:
            conn.close()

    def eliminar_grupo(self, nombre: str):
        """Elimina un grupo por nombre"""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        cursor.execute("DELETE FROM grupos WHERE nombre = ?", (nombre,))
        conn.commit()
        conn.close()

    def agregar_cliente(self, datos: Dict) -> int:
        """
        Agrega un nuevo cliente a la base de datos.
        
        Args:
            datos: Diccionario con los datos del cliente
            
        Returns:
            ID del cliente creado
        """
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        ahora = datetime.now().isoformat()
        
        cursor.execute('''
            INSERT INTO clientes (
                nombre, telefono, email, nss, empresa, puesto, 
                salario, notas, estatus, grupo, fecha_registro, fecha_actualizacion, asesor
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ''', (
            datos.get('nombre', ''),
            datos.get('telefono', ''),
            datos.get('email', ''),
            datos.get('nss', ''),
            datos.get('empresa', ''),
            datos.get('puesto', ''),
            datos.get('salario', 0.0),
            datos.get('notas', ''),
            datos.get('estatus', 'Prospecto'),
            datos.get('grupo', ''), # Nuevo campo grupo
            ahora,
            ahora,
            datos.get('asesor', '')
        ))
        
        cliente_id = cursor.lastrowid
        conn.commit()
        conn.close()
        
        return cliente_id
    
    def obtener_clientes(self, filtro: Optional[str] = None, estatus: Optional[str] = None, limite: int = 50, offset: int = 0) -> List[Dict]:
        """
        Obtiene la lista de clientes con filtros opcionales y paginación.
        
        Args:
            filtro: Texto para buscar en nombre, teléfono o email
            estatus: Filtrar por estatus específico
            limite: Cantidad máxima de registros a retornar
            offset: Desplazamiento desde el inicio
            
        Returns:
            Lista de diccionarios con datos de clientes
        """
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        cursor = conn.cursor()
        
        query = "SELECT * FROM clientes WHERE 1=1"
        params = []
        
        if filtro:
            query += " AND (nombre LIKE ? OR telefono LIKE ? OR email LIKE ? OR grupo LIKE ?)"
            filtro_param = f"%{filtro}%"
            params.extend([filtro_param, filtro_param, filtro_param, filtro_param])
        
        if estatus:
            query += " AND estatus = ?"
            params.append(estatus)
        
        query += " ORDER BY fecha_actualizacion DESC LIMIT ? OFFSET ?"
        params.extend([limite, offset])
        
        cursor.execute(query, params)
        rows = cursor.fetchall()
        
        clientes = [dict(row) for row in rows]
        conn.close()
        
        return clientes
    
    def actualizar_cliente(self, cliente_id: int, datos: Dict):
        """Actualiza los datos de un cliente existente"""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        ahora = datetime.now().isoformat()
        
        cursor.execute('''
            UPDATE clientes SET
                nombre = ?, telefono = ?, email = ?, nss = ?,
                empresa = ?, puesto = ?, salario = ?, notas = ?,
                estatus = ?, grupo = ?, fecha_actualizacion = ?
            WHERE id = ?
        ''', (
            datos.get('nombre', ''),
            datos.get('telefono', ''),
            datos.get('email', ''),
            datos.get('nss', ''),
            datos.get('empresa', ''),
            datos.get('puesto', ''),
            datos.get('salario', 0.0),
            datos.get('notas', ''),
            datos.get('estatus', 'Prospecto'),
            datos.get('grupo', ''),
            ahora,
            cliente_id
        ))
        
        conn.commit()
        conn.close()
    
    def eliminar_cliente(self, cliente_id: int):
        """Elimina un cliente de la base de datos"""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        # Eliminar interacciones asociadas
        cursor.execute("DELETE FROM interacciones WHERE cliente_id = ?", (cliente_id,))
        
        # Eliminar cliente
        cursor.execute("DELETE FROM clientes WHERE id = ?", (cliente_id,))
        
        conn.commit()
        conn.close()
    
    def agregar_interaccion(self, cliente_id: int, tipo: str, descripcion: str):
        """Registra una interacción con un cliente"""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        ahora = datetime.now().isoformat()
        
        cursor.execute('''
            INSERT INTO interacciones (cliente_id, fecha, tipo, descripcion)
            VALUES (?, ?, ?, ?)
        ''', (cliente_id, ahora, tipo, descripcion))
        
        conn.commit()
        conn.close()
    
    def obtener_estadisticas(self) -> Dict:
        """Obtiene estadísticas generales de la agenda"""
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()
        
        # Total de clientes
        cursor.execute("SELECT COUNT(*) FROM clientes")
        total = cursor.fetchone()[0]
        
        # Por estatus
        cursor.execute("SELECT estatus, COUNT(*) FROM clientes GROUP BY estatus")
        por_estatus = dict(cursor.fetchall())
        
        conn.close()
        
        return {
            'total': total,
            'por_estatus': por_estatus
        }
    
    def parsear_datos_crudos(self, texto: str) -> Dict:
        """
        Parsea texto crudo pegado y extrae información del cliente.
        Soporta formatos:
        1. Clave: Valor (en la misma línea)
        2. CLAVE
           Valor (en líneas separadas)
        
        Args:
            texto: Texto crudo con datos del cliente
            
        Returns:
            Diccionario con datos parseados
        """
        datos = {
            'nombre': '',
            'telefono': '',
            'email': '',
            'nss': '',
            'empresa': '',
            'puesto': '',
            'salario': 0.0,
            'notas': ''
        }
        
        lineas = texto.strip().split('\n')
        i = 0
        
        # Diccionario temporal para almacenar todos los campos
        campos = {}
        
        # Lista de claves conocidas (para detectar si una línea es clave o valor)
        claves_conocidas = {
            'TEL1', 'TEL', 'TELEFONO', 'TIPO1', 'TIPO', 'SUCURSAL_ASIGNADA', 'NOMBRE', 
            'MATRICULA', 'NSS', 'CURP', 'RFC', 'RFCX', 'SEXO', 'CP', 'ESTADO', 
            'CONVENIO', 'INICIATIVA', 'TIPO_DE_CREDITO', 'CAPACIDAD', 'COTIZADOR', 
            'PLAZO', 'OFERTA', 'TASA_MEN', 'TIPO_DE_PENSION', 'EMAIL', 'CORREO',
            'EMPRESA', 'PUESTO', 'CARGO', 'SALARIO', 'SUELDO', 'INGRESO'
        }
        
        while i < len(lineas):
            linea = lineas[i].strip()
            
            if not linea:
                i += 1
                continue
            
            # Formato 1: Clave: Valor (en la misma línea)
            if ':' in linea:
                clave, valor = linea.split(':', 1)
                campos[clave.strip().upper()] = valor.strip()
                i += 1
            # Formato 2: CLAVE en una línea, valor en la siguiente
            else:
                clave = linea.strip().upper()
                # Verificar si hay una siguiente línea
                if i + 1 < len(lineas):
                    siguiente = lineas[i + 1].strip()
                    # Si la siguiente línea existe, no está vacía, y NO es una clave conocida
                    if siguiente and siguiente.upper() not in claves_conocidas:
                        campos[clave] = siguiente
                        i += 2  # Saltar clave y valor
                    else:
                        # La siguiente línea es otra clave o está vacía
                        i += 1
                else:
                    i += 1
        
        # Mapear campos conocidos a los datos del cliente
        # Nombre
        if 'NOMBRE' in campos:
            datos['nombre'] = campos['NOMBRE']
        
        # Teléfono (priorizar TEL1, luego TEL, TELEFONO, CELULAR)
        for key in ['TEL1', 'TEL', 'TELEFONO', 'TELÉFONO', 'CELULAR']:
            if key in campos:
                datos['telefono'] = campos[key]
                break
        
        # Email
        for key in ['EMAIL', 'CORREO', 'MAIL']:
            if key in campos:
                datos['email'] = campos[key]
                break
        
        # NSS
        if 'NSS' in campos:
            datos['nss'] = campos['NSS']
        
        # Empresa/Convenio
        for key in ['CONVENIO', 'EMPRESA', 'COMPAÑÍA', 'SUCURSAL_ASIGNADA']:
            if key in campos:
                datos['empresa'] = campos[key]
                break
        
        # Puesto
        for key in ['PUESTO', 'CARGO', 'TIPO_DE_PENSION']:
            if key in campos:
                datos['puesto'] = campos[key]
                break
        
        # Salario/Capacidad
        for key in ['CAPACIDAD', 'SALARIO', 'SUELDO', 'INGRESO', 'OFERTA']:
            if key in campos:
                try:
                    valor_limpio = campos[key].replace('$', '').replace(',', '').strip()
                    datos['salario'] = float(valor_limpio)
                    break
                except:
                    pass
        
        # Agregar información adicional a notas
        notas_adicionales = []
        campos_importantes = ['CURP', 'RFC', 'MATRICULA', 'CP', 'ESTADO', 'PLAZO', 'TASA_MEN', 'TIPO_DE_CREDITO']
        
        for key in campos_importantes:
            if key in campos:
                notas_adicionales.append(f"{key}: {campos[key]}")
        
        if notas_adicionales:
            datos['notas'] = '\n'.join(notas_adicionales)
        
        return datos

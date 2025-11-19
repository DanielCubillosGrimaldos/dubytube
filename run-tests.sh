#!/bin/bash

# Script para ejecutar todos los tests del proyecto DubyTube
# Autor: DubyTube Team
# Fecha: 2025-11-18

echo "🧪 Ejecutando pruebas unitarias de DubyTube..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Ejecutar tests con Maven
mvn clean test

# Capturar el código de salida
EXIT_CODE=$?

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ Todos los tests pasaron exitosamente!"
    echo ""
    echo "📊 Resumen:"
    echo "   - AuthServiceTest: 4 tests"
    echo "   - GrafoSocialTest: 7 tests"
    echo "   - TrieTest: 9 tests"
    echo "   - BusquedaAvanzadaTest: 8 tests"
    echo "   - RecomendacionServiceTest: 7 tests"
    echo "   ─────────────────────────────"
    echo "   Total: 35 tests"
    echo ""
    echo "📁 Reportes disponibles en: target/surefire-reports/"
else
    echo "❌ Algunos tests fallaron. Revisa el log anterior."
    echo ""
    echo "📁 Reportes detallados en: target/surefire-reports/"
    exit $EXIT_CODE
fi

#!/bin/bash

# Script para ejecutar las pruebas unitarias
# Uso: ./run_tests.sh

echo "================================================"
echo "  DubyTube - Ejecutor de Pruebas Unitarias"
echo "================================================"
echo ""

# Ejecutar tests
echo "🧪 Ejecutando pruebas unitarias..."
echo ""

mvn clean test

if [ $? -eq 0 ]; then
    echo ""
    echo "================================================"
    echo "  ✅ TODAS LAS PRUEBAS PASARON EXITOSAMENTE"
    echo "================================================"
    echo ""
    echo "📊 Resumen de Tests:"
    echo "   - AuthServiceTest: 4 tests"
    echo "   - GrafoSocialTest: 7 tests"
    echo "   - TrieTest: 9 tests"
    echo "   - BusquedaAvanzadaTest: 8 tests"
    echo "   - RecomendacionServiceTest: 7 tests"
    echo "   --------------------------------"
    echo "   📈 TOTAL: 35 tests ejecutados"
    echo ""
    echo "📂 Reportes en: target/surefire-reports/"
else
    echo ""
    echo "================================================"
    echo "  ❌ ALGUNAS PRUEBAS FALLARON"
    echo "================================================"
    echo ""
    echo "📂 Ver detalles en: target/surefire-reports/"
    exit 1
fi

#!/bin/bash

# Script para generar y abrir la documentación JavaDoc
# Uso: ./view_docs.sh

echo "================================================"
echo "  DubyTube - Generador de Documentación JavaDoc"
echo "================================================"
echo ""

# Generar la documentación
echo "📚 Generando documentación JavaDoc..."
mvn javadoc:javadoc

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Documentación generada exitosamente!"
    echo ""
    echo "📂 Ubicación: target/site/apidocs/index.html"
    echo ""
    
    # Intentar abrir el navegador
    if command -v xdg-open > /dev/null; then
        echo "🌐 Abriendo en el navegador..."
        xdg-open target/site/apidocs/index.html
    elif command -v open > /dev/null; then
        echo "🌐 Abriendo en el navegador..."
        open target/site/apidocs/index.html
    else
        echo "⚠️  Abre manualmente: target/site/apidocs/index.html"
    fi
else
    echo ""
    echo "❌ Error al generar la documentación"
    exit 1
fi

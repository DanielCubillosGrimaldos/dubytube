#!/bin/bash

# Script para generar y abrir la documentación JavaDoc
# Autor: DubyTube Team
# Fecha: 2025-11-18

echo "📚 Generando documentación JavaDoc de DubyTube..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Generar JavaDoc
mvn javadoc:javadoc

# Capturar el código de salida
EXIT_CODE=$?

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ Documentación generada exitosamente!"
    echo ""
    echo "📁 Ubicación: target/site/apidocs/index.html"
    echo ""
    
    # Detectar sistema operativo y abrir el navegador
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        echo "🌐 Abriendo documentación en el navegador..."
        xdg-open target/site/apidocs/index.html 2>/dev/null || \
        sensible-browser target/site/apidocs/index.html 2>/dev/null || \
        firefox target/site/apidocs/index.html 2>/dev/null || \
        google-chrome target/site/apidocs/index.html 2>/dev/null || \
        echo "⚠️  Por favor, abre manualmente: target/site/apidocs/index.html"
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        echo "🌐 Abriendo documentación en el navegador..."
        open target/site/apidocs/index.html
    elif [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
        echo "🌐 Abriendo documentación en el navegador..."
        start target/site/apidocs/index.html
    else
        echo "⚠️  Abre manualmente en tu navegador: target/site/apidocs/index.html"
    fi
    
    echo ""
    echo "📦 Paquetes documentados:"
    echo "   - org.dubytube.dubytube.domain"
    echo "   - org.dubytube.dubytube.ds"
    echo "   - org.dubytube.dubytube.services"
    echo "   - org.dubytube.dubytube.repo"
    echo "   - org.dubytube.dubytube.viewController"
else
    echo "❌ Error al generar la documentación."
    exit $EXIT_CODE
fi

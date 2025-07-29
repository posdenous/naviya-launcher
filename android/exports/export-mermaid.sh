#!/bin/bash

# Naviya Mermaid Diagram Export Script
# Converts Mermaid diagrams to various formats

echo "🏥 Naviya Mermaid Export Tool"
echo "=============================="

# Check if mermaid-cli is installed
if ! command -v mmdc &> /dev/null; then
    echo "📦 Installing Mermaid CLI..."
    npm install -g @mermaid-js/mermaid-cli
fi

# Create output directories
mkdir -p svg png pdf html

echo "🎨 Exporting diagrams to multiple formats..."

# Export each diagram to SVG, PNG, and PDF
diagrams=("elderly-user-flow" "healthcare-professional-flow" "family-caregiver-flow" "emergency-response-flow")

for diagram in "${diagrams[@]}"; do
    echo "Processing: $diagram"
    
    # SVG (vector format - best quality)
    mmdc -i "${diagram}.mermaid" -o "svg/${diagram}.svg" -t neutral -b white
    
    # PNG (raster format - good for presentations)
    mmdc -i "${diagram}.mermaid" -o "png/${diagram}.png" -t neutral -b white --width 1920 --height 1080
    
    # PDF (print format)
    mmdc -i "${diagram}.mermaid" -o "pdf/${diagram}.pdf" -t neutral -b white
    
    # HTML (interactive format)
    cat > "html/${diagram}.html" << EOF
<!DOCTYPE html>
<html>
<head>
    <title>Naviya - ${diagram}</title>
    <script src="https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js"></script>
</head>
<body>
    <div class="mermaid">
$(cat "${diagram}.mermaid")
    </div>
    <script>
        mermaid.initialize({startOnLoad:true});
    </script>
</body>
</html>
EOF
done

echo "✅ Export complete!"
echo "📁 Files exported to:"
echo "   - SVG: svg/ (vector graphics)"
echo "   - PNG: png/ (high-res images)"
echo "   - PDF: pdf/ (print-ready)"
echo "   - HTML: html/ (interactive)"

# Create combined HTML with all diagrams
cat > "html/all-flows.html" << 'EOF'
<!DOCTYPE html>
<html>
<head>
    <title>Naviya - All User Flows</title>
    <script src="https://cdn.jsdelivr.net/npm/mermaid/dist/mermaid.min.js"></script>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .flow-section { margin: 40px 0; border: 1px solid #ddd; padding: 20px; }
        .flow-title { font-size: 1.5em; color: #2c5aa0; margin-bottom: 20px; }
    </style>
</head>
<body>
    <h1>🏥 Naviya User Flow Diagrams</h1>
    
    <div class="flow-section">
        <div class="flow-title">👴 Elderly User Daily Flow</div>
        <div class="mermaid">
EOF

cat elderly-user-flow.mermaid >> "html/all-flows.html"

cat >> "html/all-flows.html" << 'EOF'
        </div>
    </div>
    
    <div class="flow-section">
        <div class="flow-title">👨‍⚕️ Healthcare Professional Installation</div>
        <div class="mermaid">
EOF

cat healthcare-professional-flow.mermaid >> "html/all-flows.html"

cat >> "html/all-flows.html" << 'EOF'
        </div>
    </div>
    
    <div class="flow-section">
        <div class="flow-title">👨‍👩‍👧‍👦 Family Caregiver Monitoring</div>
        <div class="mermaid">
EOF

cat family-caregiver-flow.mermaid >> "html/all-flows.html"

cat >> "html/all-flows.html" << 'EOF'
        </div>
    </div>
    
    <div class="flow-section">
        <div class="flow-title">🚨 Emergency Response System</div>
        <div class="mermaid">
EOF

cat emergency-response-flow.mermaid >> "html/all-flows.html"

cat >> "html/all-flows.html" << 'EOF'
        </div>
    </div>
    
    <script>
        mermaid.initialize({
            startOnLoad: true,
            theme: 'neutral',
            flowchart: {
                useMaxWidth: true,
                htmlLabels: true
            }
        });
    </script>
</body>
</html>
EOF

echo "🌟 Combined interactive HTML created: html/all-flows.html"

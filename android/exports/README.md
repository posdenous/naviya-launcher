# 🏥 Naviya Mermaid User Flow Exports

This directory contains the Naviya user flow diagrams in various exportable formats, optimised for understanding user behaviour patterns.

## 📁 File Structure

```
exports/
├── *.mermaid           # Raw Mermaid diagram files
├── export-mermaid.sh   # Export script for multiple formats
├── svg/               # Vector graphics (best quality)
├── png/               # High-resolution images
├── pdf/               # Print-ready documents
└── html/              # Interactive web versions
```

## 🎯 Available User Flows

### 1. **elderly-user-flow.mermaid**
- Daily interaction patterns for elderly users
- Emergency vs normal usage decision points
- Confusion recovery mechanisms
- **Key Insight**: Shows routine-driven behaviour with safety-first design

### 2. **healthcare-professional-flow.mermaid**
- Professional installation and assessment workflow
- Clinical evaluation process (MMSE, ADL, social assessment)
- Abuse risk detection and enhanced safety protocols
- **Key Insight**: Assessment-focused with patient autonomy preservation

### 3. **family-caregiver-flow.mermaid**
- Caregiver onboarding and monitoring patterns
- Emergency response coordination
- Abuse detection and reporting mechanisms
- **Key Insight**: Alert-responsive with system boundary respect

### 4. **emergency-response-flow.mermaid**
- Multi-channel emergency activation methods
- Graduated response levels (HELP/URGENT/EMERGENCY)
- Silent panic mode for abuse situations
- **Key Insight**: Critical path analysis with covert protection

## 🚀 Quick Export

Run the export script to generate all formats:

```bash
./export-mermaid.sh
```

This will create:
- **SVG files** - Vector graphics for presentations
- **PNG files** - High-res images for documents
- **PDF files** - Print-ready versions
- **HTML files** - Interactive web versions

## 🎨 Export Formats Available

### Vector Graphics (SVG)
- **Best for**: Presentations, scalable graphics
- **Quality**: Infinite resolution
- **Use case**: Professional presentations, web embedding

### Raster Images (PNG)
- **Best for**: Documents, reports, slides
- **Quality**: 1920x1080 high resolution
- **Use case**: PowerPoint, Word documents, reports

### Print Documents (PDF)
- **Best for**: Physical printing, formal documentation
- **Quality**: Print-optimised
- **Use case**: Stakeholder reports, compliance documentation

### Interactive Web (HTML)
- **Best for**: Online documentation, interactive exploration
- **Quality**: Dynamic rendering
- **Use case**: GitHub pages, internal wikis, training materials

## 🔍 User Behaviour Analysis Features

Each diagram highlights:

### Decision Points
- **Diamond shapes** show where users make critical choices
- **Colour coding** indicates risk levels (red=emergency, green=success)
- **Multiple paths** show different user journey outcomes

### Behavioural Patterns
- **Routine flows** for daily usage patterns
- **Emergency paths** for crisis situations
- **Confusion recovery** for when users get lost
- **Abuse protection** for vulnerable user safety

### Multi-User Perspectives
- **Elderly users** - Primary interaction patterns
- **Healthcare professionals** - Clinical workflow requirements
- **Family caregivers** - Monitoring and response behaviour
- **Emergency systems** - Critical path analysis

## 📊 Integration with Other Tools

### GitHub Integration
- Mermaid diagrams render natively in GitHub markdown
- Version control friendly (text-based)
- Easy to update and maintain

### Documentation Systems
- Compatible with GitBook, Confluence, Notion
- Embeddable in wikis and knowledge bases
- Supports collaborative editing

### Presentation Tools
- SVG exports work in PowerPoint, Keynote
- PNG exports suitable for Google Slides
- PDF exports for printed handouts

## 🎯 Best Practices for User Flow Analysis

1. **Focus on Decision Points** - Where users make choices
2. **Highlight Critical Paths** - Emergency and safety routes
3. **Show Recovery Mechanisms** - How users get help when confused
4. **Colour Code Risk Levels** - Visual priority indication
5. **Multi-User Perspective** - Different stakeholder journeys

## 🛠️ Customisation

To modify diagrams:
1. Edit the `.mermaid` files directly
2. Run `./export-mermaid.sh` to regenerate all formats
3. Commit changes to version control
4. Update documentation as needed

The Mermaid syntax is human-readable and version-control friendly, making it ideal for collaborative user experience design and stakeholder communication.

---

*These user flow diagrams prioritise understanding elderly user behaviour patterns and safety-first design principles.*

# 🏥 Naviya User Flow Diagrams
*Understanding User Behaviour Through Visual Workflows*

## 👥 Primary User Types & Their Journeys

### 1. 👴 Elderly User - Daily Interaction Flow

```mermaid
flowchart TD
    A[Wake Up] --> B{Check Phone}
    B -->|Sees Naviya| C[Open Launcher]
    C --> D{Emergency Needed?}
    D -->|Yes| E[Press SOS Button]
    D -->|No| F[Check Unread Tile]
    
    E --> E1[Select Emergency Level]
    E1 --> E2[HELP/URGENT/EMERGENCY]
    E2 --> E3[Multi-channel Alert Sent]
    E3 --> E4[Wait for Response]
    
    F --> F1{Has Unread?}
    F1 -->|Yes| F2[View Messages/Calls]
    F1 -->|No| F3[Browse App Grid]
    
    F2 --> F4[Respond to Family]
    F3 --> F5[Launch Familiar App]
    F5 --> F6[Complete Task]
    F6 --> G[Return to Launcher]
    F4 --> G
    E4 --> G
    
    G --> H{Feeling Confused?}
    H -->|Yes| I[Triple-tap for Help]
    H -->|No| J[Continue Normal Use]
    
    I --> I1[Panic Mode Activated]
    I1 --> I2[Silent Help Sent]
    I2 --> I3[Normal UI Maintained]
    
    style E fill:#ff6b6b
    style I fill:#ff9999
    style E3 fill:#4ecdc4
    style I2 fill:#4ecdc4
```

### 2. 👨‍⚕️ Healthcare Professional - Installation Flow

```mermaid
flowchart TD
    A[Receive Patient Referral] --> B[Access Professional Portal]
    B --> C[Verify Credentials]
    C --> D{Credentials Valid?}
    D -->|No| E[Re-submit Documentation]
    D -->|Yes| F[Schedule Patient Visit]
    
    E --> C
    F --> G[Meet with Patient]
    G --> H[Explain Naviya System]
    H --> I{Patient Consents?}
    I -->|No| J[Document Refusal]
    I -->|Yes| K[Begin Installation]
    
    K --> L[Clinical Assessment]
    L --> L1[Cognitive Test - MMSE]
    L1 --> L2[Functional Assessment - ADL]
    L2 --> L3[Social Assessment]
    L3 --> L4[Risk Factor Evaluation]
    L4 --> M{High Abuse Risk?}
    
    M -->|Yes| N[Enhanced Safety Protocols]
    M -->|No| O[Standard Setup]
    
    N --> N1[Assign Elder Rights Advocate]
    N1 --> N2[Setup Secret Panic Mode]
    N2 --> N3[Enhanced Monitoring]
    
    O --> P[Configure Emergency Contacts]
    N3 --> P
    P --> Q[Setup Caregiver Access]
    Q --> R[Final System Test]
    R --> S[Patient Training]
    S --> T[Documentation Complete]
    
    style M fill:#ff6b6b
    style N fill:#ff9999
    style N1 fill:#4ecdc4
    style T fill:#51cf66
```

### 3. 👨‍👩‍👧‍👦 Family Caregiver - Monitoring Flow

```mermaid
flowchart TD
    A[Receive Caregiver Invite] --> B[Download Caregiver App]
    B --> C[Scan QR Code]
    C --> D[Verify Identity]
    D --> E{Elderly User Approves?}
    E -->|No| F[Request Denied]
    E -->|Yes| G[Access Granted]
    
    G --> H[View Dashboard]
    H --> I{Emergency Alert?}
    I -->|Yes| J[Emergency Response]
    I -->|No| K[Check Daily Status]
    
    J --> J1[View Emergency Details]
    J1 --> J2[Contact Emergency Services]
    J2 --> J3[Coordinate Response]
    J3 --> J4[Follow-up with User]
    
    K --> K1[App Usage Summary]
    K1 --> K2[Communication Status]
    K2 --> K3[Location Check]
    K3 --> L{Concerning Patterns?}
    
    L -->|Yes| M[Review Detailed Logs]
    L -->|No| N[Normal Monitoring]
    
    M --> M1{Abuse Indicators?}
    M1 -->|Yes| M2[System Flags Alert]
    M1 -->|No| M3[Continue Monitoring]
    
    M2 --> M4[Elder Rights Contacted]
    M4 --> M5[Investigation Initiated]
    
    style J fill:#ff6b6b
    style M2 fill:#ff9999
    style M4 fill:#4ecdc4
    style M5 fill:#51cf66
```

### 4. 🚨 Emergency Response Flow - Critical Path

```mermaid
flowchart TD
    A[Emergency Triggered] --> B{Trigger Method}
    B -->|SOS Button| C[Visible Emergency]
    B -->|Triple-tap| D[Panic Mode]
    B -->|Voice Command| C
    B -->|Shake Device| C
    B -->|Secret SMS| D
    
    C --> C1[Emergency Level Selection]
    C1 --> C2{Level Selected}
    C2 -->|HELP| C3[Assistance Needed]
    C2 -->|URGENT| C4[Medical Emergency]
    C2 -->|EMERGENCY| C5[Call 911]
    
    D --> D1[Silent Mode Activated]
    D1 --> D2[Background Location Sharing]
    D2 --> D3[Stealth Evidence Recording]
    D3 --> D4[Elder Rights Alert]
    
    C3 --> E[Notify Caregivers]
    C4 --> F[Medical Alert + Caregivers]
    C5 --> G[Emergency Services + All]
    
    E --> H[SMS + App Notification]
    F --> I[Medical Services + SMS + App]
    G --> J[911 Call + Location + Medical Info]
    
    D4 --> K[Silent SMS to Advocates]
    K --> L[Covert Location Sharing]
    L --> M[Evidence Preservation]
    
    H --> N[Caregiver Response]
    I --> N
    J --> O[Emergency Services Response]
    M --> P[Advocate Investigation]
    
    style D fill:#ff9999
    style D4 fill:#4ecdc4
    style G fill:#ff6b6b
    style O fill:#51cf66
    style P fill:#51cf66
```

## 🎨 User Behaviour Insights

### Elderly User Patterns:
- **Routine-driven**: Same apps, same times
- **Safety-first**: Emergency access always visible
- **Simplicity**: Maximum 6 tiles, large touch targets
- **Confusion recovery**: Triple-tap help mechanism

### Caregiver Behaviour:
- **Alert-responsive**: Immediate emergency response
- **Pattern-watching**: Daily usage monitoring
- **Boundary-respecting**: Limited access by design
- **Abuse-aware**: System flags concerning patterns

### Professional Workflow:
- **Assessment-focused**: Clinical evaluation first
- **Safety-prioritised**: Abuse risk evaluation
- **Documentation-heavy**: Compliance requirements
- **Patient-centred**: User autonomy preserved

## 📊 Behavioural Decision Points

### Critical User Decisions:
1. **Emergency vs Normal Use** - Primary split in user journey
2. **Consent to Monitoring** - Elderly user maintains control
3. **Abuse Risk Assessment** - Professional evaluation
4. **Panic Mode Activation** - Silent help mechanism

### System Response Patterns:
- **Graduated Emergency Response** - HELP → URGENT → EMERGENCY
- **Silent Protection Mode** - Invisible abuse prevention
- **Automatic Escalation** - Pattern-based risk detection
- **Multi-channel Communication** - Redundant alert systems

## 🔍 User Testing Scenarios

Based on these flows, key testing scenarios should include:

1. **Elderly User Confusion Recovery**
2. **Emergency Response Time Validation**
3. **Caregiver Boundary Enforcement**
4. **Abuse Pattern Detection Accuracy**
5. **Professional Installation Compliance**

---

*These diagrams prioritise user behaviour understanding and can be rendered interactively in GitHub, documentation systems, or presentation tools.*

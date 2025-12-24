# How to Control AI Agent

## 📍 Location of Rules
Your custom rules are in: **`.agent/rules.md`**

---

## ✏️ How to Edit Rules

### **Option 1: Direct Edit**
1. Open `.agent/rules.md`
2. Edit the rules
3. Save the file
4. AI will follow new rules automatically

### **Option 2: Tell AI to Update**
Just say:
```
"Update my rules to include [your new rule]"
```

---

## 🎯 Quick Rule Templates

### **To Make AI Ask Before Everything:**
```markdown
### Always Ask Permission
- Ask before creating files
- Ask before running commands
- Ask before making changes
- Wait for my "yes" or "no"
```

### **To Control Code Style:**
```markdown
### Code Style Rules
- Use tabs, not spaces
- Max line length: 100
- Always add comments
- Use specific naming: camelCase for variables
```

### **To Control Communication:**
```markdown
### Communication Style
- Only speak in Hindi
- Be very brief
- No emojis
- Technical terms only
```

### **To Control Workflow:**
```markdown
### Workflow Rules
- Show me plan first
- Get approval before coding
- Test after every change
- Commit only working code
```

---

## 🔧 Common Customizations

### **1. Language Preference**
```markdown
## Language
- Speak only in: [Hindi/English/Hinglish]
- Use technical terms in: [English]
- Explain concepts in: [Simple Hindi]
```

### **2. Approval Requirements**
```markdown
## Require Approval For:
- ✅ Creating new files
- ✅ Deleting files
- ✅ Running commands
- ✅ Installing packages
- ✅ Changing architecture
- ❌ Reading files (no approval needed)
- ❌ Explaining code (no approval needed)
```

### **3. Code Preferences**
```markdown
## Code Style
- Indentation: [Tabs/Spaces/4 spaces]
- Line length: [80/100/120]
- Comments: [Required/Optional]
- Naming: [camelCase/snake_case/PascalCase]
```

### **4. Testing Requirements**
```markdown
## Testing Rules
- Always write tests: [Yes/No]
- Test before commit: [Yes/No]
- Coverage required: [80%/90%/100%]
```

---

## 💡 Example Rules

### **Strict Control Mode:**
```markdown
# STRICT MODE

## Every Action Needs Approval
1. Ask me before EVERY file creation
2. Ask me before EVERY command
3. Ask me before EVERY code change
4. Show me the plan first
5. Wait for my explicit "yes"
6. Never assume anything

## Communication
- Explain in simple Hindi
- Show me code before writing
- Give me 2-3 options always
- Let ME decide everything
```

### **Fast Development Mode:**
```markdown
# FAST MODE

## Auto-Approve Safe Actions
- ✅ Can create files automatically
- ✅ Can run safe commands (build, test)
- ✅ Can fix obvious errors
- ❌ Still ask for architecture changes
- ❌ Still ask for deletions

## Communication
- Be brief
- Show results, not process
- Only ask for important decisions
```

---

## 🎯 How AI Reads Your Rules

The AI checks these files in order:
1. **`.agent/rules.md`** (Your custom rules) ← **HIGHEST PRIORITY**
2. System instructions (Built-in rules)

**Your rules OVERRIDE system rules!**

---

## 📝 Rule Syntax

### **Use Clear Headers:**
```markdown
## This is a main rule
### This is a sub-rule
- This is a specific point
```

### **Use Checkboxes:**
```markdown
- ✅ DO this
- ❌ DON'T do this
```

### **Use Emphasis:**
```markdown
- **ALWAYS** ask before deleting
- **NEVER** auto-run risky commands
- **MUST** test after changes
```

---

## 🔄 Updating Rules Mid-Conversation

You can tell AI:
- "Add a rule: always use TypeScript"
- "Update rule: don't use emojis"
- "Remove rule: auto-testing"
- "Follow strict mode from now"

---

## 🎨 Example Conversations

### **With Current Rules:**
```
You: "Create a new feature"
AI: "Sure! Let me explain the plan first..."
[Shows plan]
AI: "Should I proceed?"
You: "Yes"
AI: [Implements]
```

### **With Strict Rules:**
```
You: "Create a new feature"
AI: "I need to create 3 files. Can I?"
You: "Yes"
AI: "File 1: MainActivity.kt - OK?"
You: "Yes"
AI: [Creates file 1]
AI: "File 2: ViewModel.kt - OK?"
You: "Yes"
AI: [Creates file 2]
...
```

### **With Fast Rules:**
```
You: "Create a new feature"
AI: [Creates all files]
AI: [Implements feature]
AI: "Done! Feature created. Want to test?"
```

---

## 🎯 Pro Tips

1. **Start Strict, Then Relax**
   - Begin with strict rules
   - Loosen as you trust AI more

2. **Be Specific**
   - "Ask before creating files" ✅
   - "Ask sometimes" ❌

3. **Use Examples**
   - Show what you want
   - Show what you don't want

4. **Update Often**
   - Rules can change
   - Update as you learn

5. **Test Your Rules**
   - Try a small task
   - See if AI follows rules
   - Adjust if needed

---

## 🔥 Quick Commands

Tell AI to:
- `"Follow strict mode"`
- `"Follow fast mode"`
- `"Ask before everything"`
- `"Auto-approve safe actions"`
- `"Explain in Hindi only"`
- `"Be more brief"`
- `"Give more details"`

---

## 📍 Where to Put Rules

```
.agent/
├── rules.md          ← Your main rules (EDIT THIS!)
├── workflows/        ← Custom workflows
└── templates/        ← Code templates
```

---

## ✨ Remember

**Your `.agent/rules.md` file is YOUR CONTROL PANEL!**

Edit it anytime to change how AI works for you! 🎯

---

**Yaar, ab tum `.agent/rules.md` file edit karo aur apne hisaab se rules set karo!** 🚀

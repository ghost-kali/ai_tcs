export default async function addSlide(presentation, ctx) {
  const slide = presentation.slides.add();

  const colors = {
    ink: "#0F172A",
    muted: "#475569",
    navy: "#0B2E4A",
    teal: "#00A3A3",
    bg: "#FFFFFF",
    card: "#F8FAFC",
    border: "#E2E8F0",
  };

  ctx.addShape(slide, { x: 0, y: 0, w: ctx.W, h: ctx.H, fill: colors.bg });
  ctx.addShape(slide, { x: 0, y: 0, w: ctx.W, h: 10, fill: colors.teal });

  ctx.addText(slide, {
    x: 56,
    y: 34,
    w: ctx.W - 112,
    h: 44,
    text: "What I Learned",
    fontSize: 30,
    bold: true,
    color: colors.navy,
    typeface: ctx.fonts.title,
  });

  const sections = [
    {
      title: "Spring Cloud Patterns",
      icon: "network",
      bullets: [
        "API Gateway routing and global filters",
        "Service discovery with Eureka and client-side load balancing",
        "Central configuration patterns",
      ],
    },
    {
      title: "Security Engineering",
      icon: "lock",
      bullets: [
        "JWT design: claims, expiry, signature validation",
        "Centralize validation at gateway + propagate identity",
        "Role-based authorization with Spring Security",
      ],
    },
    {
      title: "Performance & Reliability",
      icon: "zap",
      bullets: [
        "Caching strategy (Redis) for repeated token validations",
        "Resilience4j concepts (timeouts, circuit breakers)",
        "Designing idempotent service APIs",
      ],
    },
    {
      title: "Engineering Practices",
      icon: "wrench",
      bullets: [
        "Clean DTO boundaries vs entity exposure",
        "Meaningful error handling and consistent API contracts",
        "Debugging distributed flows across services",
      ],
    },
  ];

  const gridX = 56;
  const gridY = 118;
  const gap = 18;
  const cols = 2;
  const cardW = (ctx.W - 56 * 2 - gap) / cols;
  const cardH = 254;

  for (let i = 0; i < sections.length; i += 1) {
    const row = Math.floor(i / cols);
    const col = i % cols;
    const x = gridX + col * (cardW + gap);
    const y = gridY + row * (cardH + gap);

    ctx.addShape(slide, { x, y, w: cardW, h: cardH, fill: colors.card, line: ctx.line(colors.border, 1) });
    await ctx.addLucideIcon(slide, { x: x + 18, y: y + 18, w: 22, h: 22, icon: sections[i].icon, color: colors.teal, strokeWidth: 2.3 });
    ctx.addText(slide, { x: x + 48, y: y + 16, w: cardW - 66, h: 26, text: sections[i].title, fontSize: 16, bold: true, color: colors.ink });
    ctx.addText(slide, {
      x: x + 18,
      y: y + 54,
      w: cardW - 36,
      h: cardH - 72,
      text: sections[i].bullets.map((v) => `• ${v}`).join("\n"),
      fontSize: 13.2,
      color: colors.muted,
    });
  }

  ctx.addText(slide, {
    x: 56,
    y: ctx.H - 36,
    w: ctx.W - 112,
    h: 22,
    text: "Slide 6 / 7",
    fontSize: 11,
    color: "#94A3B8",
    align: "right",
  });

  return slide;
}


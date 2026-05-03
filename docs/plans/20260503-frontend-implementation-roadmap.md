# TaskMaster Frontend: Complete Implementation Roadmap

## Overview

React-based web application for TaskMaster task management system. Frontend consumes REST APIs from 6 microservices (Auth, User, Organization, Task, S3, Notification) with focus on task management, dashboard analytics, and workflow automation.

**Target Users:** Team leads, project managers, individual contributors
**Key Features:** Task dashboard, assignment workflow, time tracking, dependency management, project analytics

## Context

**Backend Services & Ports:**
- Auth-service (8081): JWT tokens, login/register
- UserService (8082): User profiles, settings
- OrganizationService (8083): Organizations, employees, projects
- TaskService (8084): Task CRUD, dashboard, workflow, time tracking
- S3CloudStorage (8085): File uploads, presigned URLs
- NotificationService (8090): Email/Telegram notifications (async)

**API Base URLs (environment dependent):**
- Local: `http://localhost:8081`, `http://localhost:8082`, etc.
- Docker: `http://auth-service:8081`, etc.
- Production: `https://api.taskmaster.com`

## Technology Stack

**Framework:** React 19 (or latest stable)
**Build:** Vite (or Create React App)
**State Management:** TanStack Query (React Query) + Zustand/Context API
**UI Components:** Shadcn/ui or Material-UI v5+
**Styling:** Tailwind CSS
**HTTP Client:** Axios with interceptors for JWT token management
**Forms:** React Hook Form + Zod validation
**Charts:** Recharts (for burndown, velocity charts)
**Date/Time:** date-fns
**Testing:** Vitest + React Testing Library
**E2E:** Playwright or Cypress

## Architecture

### Directory Structure

```
frontend/
├── public/
├── src/
│   ├── components/
│   │   ├── auth/
│   │   │   ├── LoginForm.tsx
│   │   │   ├── RegisterForm.tsx
│   │   │   └── ProtectedRoute.tsx
│   │   ├── common/
│   │   │   ├── Navbar.tsx
│   │   │   ├── Sidebar.tsx
│   │   │   ├── LoadingSpinner.tsx
│   │   │   └── ErrorBoundary.tsx
│   │   ├── dashboard/
│   │   │   ├── ProjectDashboard.tsx
│   │   │   ├── SprintDashboard.tsx
│   │   │   ├── TaskListWidget.tsx
│   │   │   ├── StatsCard.tsx
│   │   │   └── TaskSearchBar.tsx
│   │   ├── tasks/
│   │   │   ├── TaskCard.tsx
│   │   │   ├── TaskDetailModal.tsx
│   │   │   ├── TaskForm.tsx
│   │   │   ├── TaskAssignModal.tsx
│   │   │   ├── TaskStatusTransition.tsx
│   │   │   ├── WatchersList.tsx
│   │   │   └── CommentSection.tsx
│   │   ├── time-tracking/
│   │   │   ├── TimeLogModal.tsx
│   │   │   ├── EstimateForm.tsx
│   │   │   ├── BurndownChart.tsx
│   │   │   └── VelocityChart.tsx
│   │   └── dependencies/
│   │       ├── BlockingTasksModal.tsx
│   │       ├── CriticalPathView.tsx
│   │       └── DependencyGraph.tsx
│   ├── hooks/
│   │   ├── useAuth.ts
│   │   ├── useTasks.ts
│   │   ├── useTaskFilters.ts
│   │   ├── useTimeTracking.ts
│   │   └── useDependencies.ts
│   ├── services/
│   │   ├── api/
│   │   │   ├── auth.ts
│   │   │   ├── tasks.ts
│   │   │   ├── projects.ts
│   │   │   └── axiosConfig.ts (JWT interceptors)
│   │   └── storage/
│   │       └── tokenStorage.ts
│   ├── store/
│   │   ├── authStore.ts (Zustand)
│   │   ├── filterStore.ts (current filters)
│   │   └── uiStore.ts (modals, sidebars)
│   ├── types/
│   │   ├── api.ts (backend DTOs)
│   │   ├── domain.ts (frontend domain models)
│   │   └── ui.ts (component props types)
│   ├── pages/
│   │   ├── LoginPage.tsx
│   │   ├── RegisterPage.tsx
│   │   ├── DashboardPage.tsx
│   │   ├── ProjectPage.tsx
│   │   ├── SprintPage.tsx
│   │   ├── MyTasksPage.tsx
│   │   ├── TaskDetailPage.tsx
│   │   └── SettingsPage.tsx
│   ├── utils/
│   │   ├── dateUtils.ts
│   │   ├── formatters.ts
│   │   └── validators.ts
│   ├── App.tsx
│   ├── App.css
│   └── main.tsx
├── tests/
│   ├── unit/
│   └── e2e/
├── .env.example
├── vite.config.ts
├── tailwind.config.js
└── package.json
```

### State Management Architecture

**Zustand stores:**
```
authStore: { user, token, refreshToken, login, logout, setToken }
filterStore: { projectId, sprintId, statusFilter, priorityFilter, assigneeFilter }
uiStore: { modals: { taskDetail, assign, timeLog }, sidebarOpen }
```

**TanStack Query (React Query):**
- Cache API responses with automatic refetching
- Manage loading/error states globally
- Mutations for create/update/delete operations

## API Endpoints Consumed

### Authentication (Auth-service)
```
POST   /auth/login           — login with email/password → { accessToken, refreshToken, user }
POST   /auth/register        — register new account → { user }
POST   /auth/refresh         — refresh access token → { accessToken }
GET    /auth/profile         — get current user profile
```

### Tasks (TaskService)
```
# CRUD
POST   /api/tasks                          — create task
GET    /api/tasks/{id}                     — get task details
PUT    /api/tasks/{id}                     — update task
DELETE /api/tasks/{id}                     — delete task
GET    /api/tasks                          — list all tasks (with ?projectId=...)
GET    /api/tasks/search                   — search tasks with filters (POST)

# Phase 1: Dashboard & Analytics
GET    /api/tasks/my                       — my assigned tasks
GET    /api/tasks/search-text?q=X&projectId=X — search by name/description
GET    /api/projects/{projectId}/tasks/dashboard — project dashboard stats
GET    /api/sprints/{sprintId}/dashboard   — sprint dashboard with burndown

# Phase 2: Assignment & Workflow
POST   /api/tasks/{taskId}/assign          — assign task to user
POST   /api/tasks/{taskId}/unassign        — unassign task
POST   /api/tasks/{taskId}/watchers        — add watcher
DELETE /api/tasks/{taskId}/watchers/{watcherId} — remove watcher
POST   /api/tasks/{taskId}/transition      — change status (TODO → IN_PROGRESS → DONE)
GET    /api/tasks/{taskId}/history         — task history/comments/changes
POST   /api/tasks/{taskId}/comments        — add comment

# Phase 3: Time Tracking (to implement)
POST   /api/tasks/{taskId}/estimate        — set estimated hours
POST   /api/tasks/{taskId}/log-time        — log time spent
GET    /api/sprints/{sprintId}/burndown    — burndown chart data
GET    /api/projects/{projectId}/velocity  — velocity data

# Phase 4: Dependencies & Blocking (to implement)
POST   /api/tasks/{taskId}/block           — block task by another
POST   /api/tasks/{taskId}/unblock/{blockingId} — unblock
GET    /api/tasks/{taskId}/blocking        — get blocking tasks
GET    /api/tasks/{taskId}/blocked-by      — get blocked tasks
GET    /api/projects/{projectId}/critical-path — critical path analysis
```

### Projects (OrganizationService)
```
GET    /api/projects/{projectId}           — get project details
GET    /api/projects                        — list user's projects
PUT    /api/projects/{projectId}           — update project
```

### Sprints (OrganizationService or TaskService)
```
GET    /api/sprints/{sprintId}             — get sprint details
GET    /api/sprints                        — list sprints
POST   /api/sprints                        — create sprint
PUT    /api/sprints/{sprintId}             — update sprint
GET    /api/sprints/{sprintId}/tasks       — get sprint tasks
```

### Users (UserService)
```
GET    /users/{userId}                     — get user profile
PUT    /users/{userId}                     — update profile
GET    /users                              — search users (for assignment)
```

### Files (S3CloudStorage)
```
POST   /api/s3/presigned-upload-url        — get presigned URL for upload
POST   /api/s3/presigned-download-url      — get presigned URL for download
```

## Feature Implementation Map

### Feature 1: Authentication & Navigation

**Pages:**
- LoginPage: Email + password form, forgot password link
- RegisterPage: Email + password + name form
- ProtectedRoute wrapper: Redirect to login if token missing

**Components:**
- Navbar: User menu (Profile → Settings → Logout), search bar, notifications icon
- Sidebar: Navigation (Dashboard, My Tasks, Projects, Teams)
- ErrorBoundary: Catch and display errors gracefully

**Endpoints:**
- `POST /auth/login` → store JWT tokens in localStorage + Zustand
- `POST /auth/register` → redirect to login
- `POST /auth/refresh` → auto-refresh token on 401 response (axios interceptor)

**Logic:**
- Add Authorization header to all requests: `Authorization: Bearer ${token}`
- Add X-User-Id header: `X-User-Id: ${currentUser.id}`
- Intercept 401 errors → refresh token → retry request
- Intercept 403 errors → redirect to unauthorized page

---

### Feature 2: Dashboard & Analytics (Phase 1)

**Pages:**
- DashboardPage: Homepage with quick stats and task widgets
- ProjectPage: Project overview with dashboard stats

**Components:**
- ProjectDashboard: Stats cards (total, todo, in-progress, done, % complete)
- SprintDashboard: Sprint stats + overdue tasks list
- TaskListWidget: Recent tasks, tasks by status
- TaskSearchBar: Search by name/description (with autocomplete)
- StatsCard: Reusable card for count display (todo: 12, in-progress: 5, etc.)

**Endpoints:**
- `GET /api/tasks/my` → fetch my assigned tasks
- `GET /api/projects/{projectId}/tasks/dashboard` → fetch project stats
- `GET /api/sprints/{sprintId}/dashboard` → fetch sprint stats
- `GET /api/tasks/search-text?q=X&projectId=X` → search tasks

**UI Flow:**
1. User lands on DashboardPage
2. Load current user's projects from localStorage or `/api/projects`
3. Render StatsCard for each status (TODO, IN_PROGRESS, DONE) with counts
4. Render TaskListWidget showing recent/active tasks
5. TaskSearchBar: onChange → debounced API call to search-text endpoint
6. Click task → open TaskDetailModal

---

### Feature 3: Task Management (Phase 1-2)

**Pages:**
- MyTasksPage: All tasks assigned to current user with filters
- TaskDetailPage: Full task view with assignment, watchers, history

**Components:**
- TaskCard: Minimal task view (name, status, assignee, priority, deadline)
- TaskDetailModal: Full task details, editable fields
- TaskForm: Create/edit task form
- TaskAssignModal: Dropdown to select assignee + add watchers
- TaskStatusTransition: Status selector (TODO → IN_PROGRESS → DONE)
- WatchersList: List of watchers, remove button
- CommentSection: Show history + form to add comment

**Endpoints:**
- `GET /api/tasks` → fetch all tasks (optionally filter by projectId)
- `GET /api/tasks/{id}` → fetch task details
- `POST /api/tasks` → create task (modal form)
- `PUT /api/tasks/{id}` → update task fields
- `POST /api/tasks/{taskId}/assign` → { assigneeId } → assign to user
- `POST /api/tasks/{taskId}/unassign` → remove assignee
- `POST /api/tasks/{taskId}/watchers` → { watcherId } → add watcher
- `DELETE /api/tasks/{taskId}/watchers/{watcherId}` → remove watcher
- `POST /api/tasks/{taskId}/transition` → { status } → change status
- `GET /api/tasks/{taskId}/history` → fetch comments/changes
- `POST /api/tasks/{taskId}/comments` → { text } → add comment

**UI Flow:**
1. MyTasksPage loads and fetches tasks from `GET /api/tasks/my`
2. Render TaskCard for each task
3. Click TaskCard → open TaskDetailModal
4. In modal:
   - Click "Assign" → TaskAssignModal (search users by name)
   - Click "Add watcher" → WatchersList (add/remove watchers)
   - Click status button → TaskStatusTransition dropdown
   - Scroll down → CommentSection (show history, form to comment)
   - Edit name/description → auto-save on blur
5. Create new task:
   - Click "New Task" button → TaskForm modal
   - Fill fields (name, description, project, sprint, priority, deadline, assignee)
   - Submit → `POST /api/tasks` → redirect to task detail

---

### Feature 4: Assignment & Workflow (Phase 2)

**Components:**
- TaskAssignModal: User search dropdown, avatar display, remove button
- WatchersList: Add/remove watchers for task
- CommentSection: Display history with HistoryAction (CREATED, UPDATED, COMMENT), add comment form
- TaskStatusTransition: Dropdown/buttons to transition status with confirmation

**Endpoints:**
- `POST /api/tasks/{taskId}/assign` → { assigneeId }
- `POST /api/tasks/{taskId}/unassign`
- `POST /api/tasks/{taskId}/watchers` → { watcherId }
- `DELETE /api/tasks/{taskId}/watchers/{watcherId}`
- `POST /api/tasks/{taskId}/transition` → { status }
- `GET /api/tasks/{taskId}/history` → fetch all changes/comments
- `POST /api/tasks/{taskId}/comments` → { text }

**UI Flow:**
1. TaskDetailModal opens
2. User clicks "Assign" button → TaskAssignModal:
   - Text input with autocomplete (search `/users?search=X`)
   - Show current assignee's avatar
   - Select new user → POST `/api/tasks/{taskId}/assign` → update modal
3. User clicks "Add Watcher" → WatchersList:
   - Search users by name (same autocomplete)
   - Add button → POST `/api/tasks/{taskId}/watchers` → refetch task
   - Remove button next to each watcher → DELETE endpoint
4. User clicks "In Progress" button → TaskStatusTransition:
   - Show dropdown with allowed statuses (TODO, IN_PROGRESS, DONE)
   - Select new status → confirmation dialog
   - POST `/api/tasks/{taskId}/transition` → { status: "IN_PROGRESS" } → update modal
5. CommentSection:
   - Load `GET /api/tasks/{taskId}/history` → display timeline of changes
   - Show who changed what and when (HistoryAction: CREATED, UPDATED, COMMENT)
   - Comment text input → POST `/api/tasks/{taskId}/comments` → append to history

---

### Feature 5: Time Tracking (Phase 3 — Backend to implement)

**Pages:**
- TimeTrackingPage: View sprint burndown and velocity

**Components:**
- TimeLogModal: Form to log time spent
- EstimateForm: Set estimated hours for task
- BurndownChart: Recharts line chart (X: days, Y: remaining hours)
- VelocityChart: Bar chart (X: sprint, Y: completed points/hours)

**Endpoints:**
- `POST /api/tasks/{taskId}/estimate` → { estimatedHours }
- `POST /api/tasks/{taskId}/log-time` → { hours }
- `GET /api/sprints/{sprintId}/burndown` → { data: { day, remaining, ideal } }
- `GET /api/projects/{projectId}/velocity` → { data: { sprint, completed, estimate } }

**UI Flow:**
1. In TaskDetailModal, user clicks "Log Time" → TimeLogModal:
   - Input field for hours
   - Submit → POST `/api/tasks/{taskId}/log-time` → show success toast
2. In TaskDetailModal, user clicks "Set Estimate" → EstimateForm:
   - Input field for estimated hours
   - Submit → POST `/api/tasks/{taskId}/estimate` → update task display
3. SprintPage shows BurndownChart:
   - Fetch `GET /api/sprints/{sprintId}/burndown`
   - Render Recharts LineChart with actual vs ideal lines
4. ProjectPage shows VelocityChart:
   - Fetch `GET /api/projects/{projectId}/velocity`
   - Render Recharts BarChart grouped by sprint

---

### Feature 6: Dependencies & Blocking (Phase 4 — Backend to implement)

**Components:**
- BlockingTasksModal: Show which tasks block this one, add new blocking relationship
- CriticalPathView: List of critical tasks in dependency chain
- DependencyGraph: Visual graph of task dependencies (optional, can use Cytoscape or Dagre)

**Endpoints:**
- `POST /api/tasks/{taskId}/block` → { blockingTaskId }
- `POST /api/tasks/{taskId}/unblock/{blockingTaskId}`
- `GET /api/tasks/{taskId}/blocking` → list blocking tasks
- `GET /api/tasks/{taskId}/blocked-by` → list blocked tasks
- `GET /api/projects/{projectId}/critical-path` → critical path analysis

**UI Flow:**
1. In TaskDetailModal, user clicks "Blocking Tasks" → BlockingTasksModal:
   - Show list of tasks that block this one
   - Search input to find task to add as blocker
   - Select task → POST `/api/tasks/{taskId}/block` → { blockingTaskId }
   - Remove button next to each blocking task → POST `/api/tasks/{taskId}/unblock`
2. ProjectPage shows CriticalPathView:
   - Fetch `GET /api/projects/{projectId}/critical-path`
   - Render list of critical tasks with dependency chain highlighted
3. Optional: TaskDetailModal shows DependencyGraph:
   - Fetch `/api/tasks/{taskId}/blocking` and `/api/tasks/{taskId}/blocked-by`
   - Render interactive dependency graph using Cytoscape

---

## Key Implementation Details

### JWT Token Management

**Storage:**
```typescript
// tokenStorage.ts
export const saveTokens = (accessToken: string, refreshToken: string) => {
  localStorage.setItem('accessToken', accessToken);
  localStorage.setItem('refreshToken', refreshToken);
};

export const getAccessToken = () => localStorage.getItem('accessToken');
export const getRefreshToken = () => localStorage.getItem('refreshToken');
export const clearTokens = () => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
};
```

**Axios Interceptors:**
```typescript
// axiosConfig.ts
const axiosInstance = axios.create({ baseURL: API_BASE_URL });

// Request interceptor: add headers
axiosInstance.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  const userId = authStore.user?.id;
  if (userId) {
    config.headers['X-User-Id'] = userId;
  }
  return config;
});

// Response interceptor: refresh token on 401
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      const refreshToken = getRefreshToken();
      if (refreshToken) {
        try {
          const { data } = await axios.post('/auth/refresh', { refreshToken });
          saveTokens(data.accessToken, data.refreshToken);
          return axiosInstance(error.config); // retry original request
        } catch {
          authStore.logout(); // refresh failed, force logout
        }
      }
    }
    return Promise.reject(error);
  }
);
```

### API Service Layer

```typescript
// services/api/tasks.ts
export const tasksAPI = {
  // CRUD
  getAll: (projectId?: UUID) => axiosInstance.get('/tasks', { params: { projectId } }),
  getById: (id: UUID) => axiosInstance.get(`/tasks/${id}`),
  create: (data: CreateTaskRequest) => axiosInstance.post('/tasks', data),
  update: (id: UUID, data: UpdateTaskRequest) => axiosInstance.put(`/tasks/${id}`, data),
  delete: (id: UUID) => axiosInstance.delete(`/tasks/${id}`),

  // Phase 1
  getMyTasks: () => axiosInstance.get('/tasks/my'),
  searchTasks: (projectId: UUID, query: string) => 
    axiosInstance.get('/tasks/search-text', { params: { projectId, q: query } }),
  getProjectDashboard: (projectId: UUID) =>
    axiosInstance.get(`/projects/${projectId}/tasks/dashboard`),
  getSprintDashboard: (sprintId: UUID) =>
    axiosInstance.get(`/sprints/${sprintId}/dashboard`),

  // Phase 2
  assignTask: (taskId: UUID, assigneeId: UUID) =>
    axiosInstance.post(`/tasks/${taskId}/assign`, { assigneeId }),
  unassignTask: (taskId: UUID) =>
    axiosInstance.post(`/tasks/${taskId}/unassign`),
  addWatcher: (taskId: UUID, watcherId: UUID) =>
    axiosInstance.post(`/tasks/${taskId}/watchers`, { watcherId }),
  removeWatcher: (taskId: UUID, watcherId: UUID) =>
    axiosInstance.delete(`/tasks/${taskId}/watchers/${watcherId}`),
  transitionStatus: (taskId: UUID, status: TaskStatus) =>
    axiosInstance.post(`/tasks/${taskId}/transition`, { status }),
  getHistory: (taskId: UUID) =>
    axiosInstance.get(`/tasks/${taskId}/history`),
  addComment: (taskId: UUID, text: string) =>
    axiosInstance.post(`/tasks/${taskId}/comments`, { text }),
};
```

### React Query Hooks

```typescript
// hooks/useTasks.ts
export const useTasks = (projectId?: UUID) => {
  return useQuery({
    queryKey: ['tasks', projectId],
    queryFn: () => tasksAPI.getAll(projectId),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

export const useTaskDetail = (taskId: UUID) => {
  return useQuery({
    queryKey: ['task', taskId],
    queryFn: () => tasksAPI.getById(taskId),
  });
};

export const useAssignTask = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ taskId, assigneeId }: { taskId: UUID; assigneeId: UUID }) =>
      tasksAPI.assignTask(taskId, assigneeId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['task'] });
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
    },
  });
};
```

### Component Example: TaskDetailModal

```typescript
// components/tasks/TaskDetailModal.tsx
export const TaskDetailModal = ({ taskId, open, onClose }: Props) => {
  const { data: task, isLoading } = useTaskDetail(taskId);
  const { data: history } = useHistory(taskId);
  const assignMutation = useAssignTask();
  const watcherMutation = useAddWatcher();
  const transitionMutation = useTransitionStatus();

  if (isLoading) return <LoadingSpinner />;
  if (!task) return null;

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>{task.name}</DialogTitle>
        </DialogHeader>

        {/* Task info */}
        <div className="space-y-4">
          <p>{task.description}</p>

          {/* Assignment section */}
          <div className="flex items-center justify-between">
            <span>Assigned to:</span>
            <button onClick={() => setShowAssignModal(true)}>
              {task.executor ? getUserName(task.executor) : 'Unassigned'}
            </button>
          </div>

          {/* Status transition */}
          <div>
            <label>Status:</label>
            <select value={task.status} onChange={(e) => 
              transitionMutation.mutate({ taskId, status: e.target.value })
            }>
              <option value="TODO">TODO</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="DONE">Done</option>
            </select>
          </div>

          {/* Watchers */}
          <div>
            <h4>Watchers:</h4>
            <WatchersList watchers={task.watchers} taskId={taskId} />
          </div>

          {/* Comments */}
          <CommentSection history={history} taskId={taskId} />
        </div>

        {/* Modals */}
        {showAssignModal && (
          <TaskAssignModal
            taskId={taskId}
            onClose={() => setShowAssignModal(false)}
            onAssign={(userId) => assignMutation.mutate({ taskId, assigneeId: userId })}
          />
        )}
      </DialogContent>
    </Dialog>
  );
};
```

## Testing Strategy

**Unit Tests:**
- API service functions (mock axios)
- Hook logic (useQuery, useMutation behavior)
- Utility functions (formatters, validators)
- Component rendering with mocked data

**E2E Tests (Playwright/Cypress):**
- Login flow → dashboard loads
- Search task → navigate to detail → assign → check status changed
- Create task → assign → add watcher → transition status → verify in history
- Time tracking: log hours → see update in task
- Dependencies: block task A by B → verify circular detection error

**Example Playwright test:**
```typescript
// tests/e2e/task-workflow.spec.ts
test('complete task workflow', async ({ page }) => {
  // Login
  await page.goto('/login');
  await page.fill('input[name="email"]', 'user@example.com');
  await page.fill('input[name="password"]', 'password');
  await page.click('button:has-text("Login")');
  
  // Wait for dashboard
  await page.waitForURL('/dashboard');
  
  // Create task
  await page.click('button:has-text("New Task")');
  await page.fill('input[name="name"]', 'Test Task');
  await page.fill('textarea[name="description"]', 'Description');
  await page.click('button:has-text("Create")');
  
  // Assign task
  await page.click('button:has-text("Assign")');
  await page.fill('input[placeholder="Search users"]', 'john');
  await page.click('text=John Doe');
  
  // Verify assignment
  await expect(page.locator('text=Assigned to: John Doe')).toBeVisible();
});
```

## Implementation Phases

### Phase 1: Authentication & Navigation (Week 1)
- [ ] Setup Vite + React + Tailwind + shadcn/ui
- [ ] Create auth store (Zustand)
- [ ] Login/Register pages with form validation
- [ ] JWT token management with axios interceptors
- [ ] Protected routes and redirect logic
- [ ] Navbar + Sidebar navigation
- [ ] Profile settings page

### Phase 2: Dashboard & Task Listing (Week 2)
- [ ] Dashboard page with stats cards
- [ ] Task list view with filters (status, priority, assignee)
- [ ] Task search with autocomplete
- [ ] Project dashboard with stats
- [ ] Sprint dashboard with task overview
- [ ] React Query setup for data fetching

### Phase 3: Task Management (Week 3)
- [ ] Task detail modal/page
- [ ] Create/edit task form
- [ ] Assign task modal
- [ ] Watchers list with add/remove
- [ ] Status transition dropdown
- [ ] Comment section with history display
- [ ] Task card components

### Phase 4: Time Tracking (Week 4)
- [ ] Time log modal
- [ ] Estimate form
- [ ] Burndown chart (Recharts)
- [ ] Velocity chart
- [ ] Sprint time tracking view
- [ ] Integrate with TaskDetailModal

### Phase 5: Dependencies & Advanced Features (Week 5)
- [ ] Blocking tasks modal
- [ ] Critical path view
- [ ] Dependency graph visualization (optional)
- [ ] Advanced filters and saved views
- [ ] Notifications/alerts
- [ ] Mobile responsive design

### Phase 6: Testing & Polish (Week 6)
- [ ] Unit tests for hooks and services
- [ ] E2E tests for critical workflows
- [ ] Performance optimization (code splitting, lazy loading)
- [ ] Error handling and retry logic
- [ ] Loading states and skeletons
- [ ] Accessibility (a11y) audit

## Deployment & Environment Setup

**Environment Variables (.env):**
```
VITE_API_BASE_URL=http://localhost:8081
VITE_API_AUTH_URL=http://localhost:8081
VITE_API_USER_URL=http://localhost:8082
VITE_API_TASK_URL=http://localhost:8084
VITE_API_PROJECT_URL=http://localhost:8083
VITE_APP_VERSION=1.0.0
```

**Build & Deploy:**
- Development: `npm run dev` → Vite dev server
- Production: `npm run build` → build to `/dist`
- Docker: Multi-stage build (build stage → nginx stage)
- Deploy to: Vercel, Netlify, or custom server

**Nginx config for SPA routing:**
```nginx
location / {
  try_files $uri $uri/ /index.html;
}
```

## Performance Considerations

- **Code splitting:** Lazy load route components with React.lazy()
- **Image optimization:** Compress avatars, use WebP with fallback
- **Caching:** staleTime 5min for task lists, immediate for user-initiated refreshes
- **Debouncing:** Search input debounced 300ms before API call
- **Virtual scrolling:** Large task lists use react-window for performance
- **API batching:** Group multiple requests (optional: GraphQL batching)

## Security Considerations

- **XSS prevention:** Use React's built-in escaping, DOMPurify for user-generated content
- **CSRF tokens:** Include in POST/PUT/DELETE requests if backend requires
- **HTTPS only:** Set secure flag on cookies
- **Token expiry:** Auto-refresh tokens, force logout on 401
- **Rate limiting:** Implement client-side rate limiting for API calls
- **Sensitive data:** Never log tokens or passwords to console

## Success Criteria

1. ✅ User can login/register with email/password
2. ✅ Dashboard shows task statistics and recent tasks
3. ✅ User can assign tasks and track status changes
4. ✅ Comments and history are visible in task detail
5. ✅ Time tracking endpoints work (estimate, log time)
6. ✅ Burndown and velocity charts display correctly
7. ✅ Dependency blocking prevents circular references
8. ✅ All E2E workflows pass
9. ✅ Mobile responsive design
10. ✅ Performance: Dashboard loads <2s, task detail <1s

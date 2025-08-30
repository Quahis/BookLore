import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Button} from 'primeng/button';
import {InputText} from 'primeng/inputtext';
import {API_CONFIG} from '../../config/api-config';
import {Tooltip} from 'primeng/tooltip';
import {TableModule} from 'primeng/table';
import {Dialog} from 'primeng/dialog';
import {FormsModule} from '@angular/forms';
import {ConfirmDialog} from 'primeng/confirmdialog';
import {ConfirmationService, MessageService} from 'primeng/api';
import {OpdsUserV2, OpdsUserV2CreateRequest, OpdsV2Service} from './opds-v2.service';
import {catchError, filter, takeUntil, tap} from 'rxjs/operators';
import {UserService} from '../user-management/user.service';
import {of, Subject} from 'rxjs';
import {Password} from 'primeng/password';

@Component({
  selector: 'app-opds-settings-v2',
  imports: [
    CommonModule,
    Button,
    InputText,
    Tooltip,
    Dialog,
    FormsModule,
    ConfirmDialog,
    TableModule,
    Password
  ],
  providers: [ConfirmationService, MessageService],
  templateUrl: './opds-settings-v2.html',
  styleUrl: './opds-settings-v2.scss'
})
export class OpdsSettingsV2 implements OnInit, OnDestroy {
  
  opdsEndpoint = `${API_CONFIG.BASE_URL}/api/v1/opds/catalog`;
  
  private opdsService = inject(OpdsV2Service);
  private confirmationService = inject(ConfirmationService);
  private messageService = inject(MessageService);
  private userService = inject(UserService);
  
  users: OpdsUserV2[] = [];
  loading = false;
  showCreateUserDialog = false;
  newUser: OpdsUserV2CreateRequest = {username: '', password: ''};
  passwordVisibility: boolean[] = [];
  hasPermission = false;
  
  private readonly destroy$ = new Subject<void>();
  dummyPassword: string = "***********************";
  
  ngOnInit(): void {
    this.loading = true;
    
    this.userService.userState$.pipe(
      filter(state => !!state?.user && state.loaded),
      takeUntil(this.destroy$),
      tap(state => {
        this.hasPermission = !!(state.user?.permissions.canAccessOpds || state.user?.permissions.admin);
      }),
      filter(() => this.hasPermission),
      tap(() => this.loadUsers())
    ).subscribe();
  }
  
  private loadUsers(): void {
    this.opdsService.getUser().pipe(
      takeUntil(this.destroy$),
      catchError(err => {
        console.error('Error loading users:', err);
        this.showMessage('error', 'Error', 'Failed to load users');
        return of([]);
      })
    ).subscribe(users => {
      this.users = users;
      this.passwordVisibility = new Array(users.length).fill(false);
      this.loading = false;
    });
  }
  
  createUser(): void {
    if (!this.newUser.username || !this.newUser.password) return;
    
    this.opdsService.createUser(this.newUser).pipe(
      takeUntil(this.destroy$),
      catchError(err => {
        console.error('Error creating user:', err);
        this.showMessage('error', 'Error', 'Failed to create user');
        return of(null);
      })
    ).subscribe(user => {
      if (!user) return;
      this.users.push(user);
      this.resetCreateUserDialog();
      this.showMessage('success', 'Success', 'User created successfully');
    });
  }
  
  confirmDelete(user: OpdsUserV2): void {
    this.confirmationService.confirm({
      message: `Are you sure you want to delete user "${user.username}"?`,
      header: 'Delete Confirmation',
      icon: 'pi pi-exclamation-triangle',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.deleteUser(user)
    });
  }
  
  deleteUser(user: OpdsUserV2): void {
    if (!user.id) return;
    
    this.opdsService.deleteCredential(user.id).pipe(
      takeUntil(this.destroy$),
      catchError(err => {
        console.error('Error deleting user:', err);
        this.showMessage('error', 'Error', 'Failed to delete user');
        return of(null);
      })
    ).subscribe(() => {
      this.users = this.users.filter(u => u.id !== user.id);
      this.showMessage('success', 'Success', 'User deleted successfully');
    });
  }
  
  cancelCreateUser(): void {
    this.resetCreateUserDialog();
  }
  
  copyEndpoint(): void {
    navigator.clipboard.writeText(this.opdsEndpoint).then(() => {
      this.showMessage('success', 'Copied', 'OPDS endpoint copied to clipboard');
    });
  }
  
  private resetCreateUserDialog(): void {
    this.showCreateUserDialog = false;
    this.newUser = {username: '', password: ''};
  }
  
  private showMessage(severity: string, summary: string, detail: string): void {
    this.messageService.add({severity, summary, detail});
  }
  
  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
